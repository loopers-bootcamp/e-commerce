package com.loopers.interfaces.listener.order;

import com.loopers.domain.order.ExternalOrderSender;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderResult;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.event.OrderEvent;
import com.loopers.domain.product.ProductResult;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.event.ProductEvent;
import com.loopers.domain.product.event.ProductEventPublisher;
import com.loopers.support.annotation.Inboxing;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final ExternalOrderSender externalOrderSender;
    private final OrderService orderService;
    private final ProductService productService;
    private final ProductEventPublisher productEventPublisher;

    /**
     * {@link Async}: 메인 트랜잭션이 동기적으로 기다려야 할 필요 없음.
     * <p>
     * {@link TransactionPhase#AFTER_COMMIT}: 메인 트랜잭션이 성공해야만 수행하는 로직이다.
     */
    @Async
    @Inboxing(idempotent = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendOrder(OrderEvent.Complete event) {
        externalOrderSender.sendOrder(event.orderId());

        OrderCommand.GetOrderDetail orderCommand = OrderCommand.GetOrderDetail.builder()
                .orderId(event.orderId())
                .build();
        OrderResult.GetOrderDetail order = orderService.getOrderDetail(orderCommand)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));

        List<Long> productOptionIds = order.getProducts().stream().map(OrderResult.GetOrderDetail.Product::getProductOptionId).toList();
        ProductResult.GetProductOptions options = productService.getProductOptions(productOptionIds)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));

        Map<Long, Integer> optionQuantityMap = order.getProducts()
                .stream()
                .collect(toMap(OrderResult.GetOrderDetail.Product::getProductOptionId, OrderResult.GetOrderDetail.Product::getQuantity));

        Map<Long, Integer> quantityAgg = new HashMap<>();
        for (ProductResult.GetProductOptions.Item item : options.getItems()) {
            Long productId = item.getProductId();
            Integer quantity = optionQuantityMap.get(item.getProductOptionId());

            quantityAgg.compute(productId, (k, v) -> v == null ? quantity : v + quantity);
        }

        quantityAgg.forEach((productId, quantity) -> productEventPublisher.publishEvent(
                new ProductEvent.Sale(event.orderId(), productId, quantity)
        ));
    }

}
