package com.loopers.domain.order;

import com.loopers.annotation.ReadOnlyTransactional;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    @ReadOnlyTransactional
    public Optional<OrderResult.GetOrderDetail> getOrderDetail(UUID orderId) {
        return orderRepository.findOrderDetailById(orderId)
                .map(OrderResult.GetOrderDetail::from);
    }

    @Transactional
    public OrderResult.Create create(OrderCommand.Create command) {
        if (CollectionUtils.isEmpty(command.getProducts())) {
            throw new BusinessException(CommonErrorType.INVALID, "주문할 상품이 없습니다.");
        }

        UUID id = orderRepository.findNextOrderId();

        Order order = Order.builder()
                .id(id)
                .totalPrice(command.getTotalPrice())
                .userId(command.getUserId())
                .build();

        List<OrderProduct> products = command.getProducts()
                .stream()
                .map(product -> OrderProduct.builder()
                        .price(product.getPrice())
                        .quantity(product.getQuantity())
                        .orderId(order.getId())
                        .productOptionId(product.getProductOptionId())
                        .build())
                .toList();
        order.addProducts(products);

        orderRepository.save(order);

        return OrderResult.Create.from(order);
    }

    @Transactional
    public void complete(UUID orderId) {
        Order order = orderRepository.findOneForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));

        order.complete();

        orderRepository.save(order);
        orderEventPublisher.complete(orderId);
    }

    @Transactional
    public void expire(UUID orderId) {
        Order order = orderRepository.findOneForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));

        order.expire();

        orderRepository.save(order);
    }

    @Transactional
    public void cancel(UUID orderId) {
        Order order = orderRepository.findOneForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));

        order.cancel();

        orderRepository.save(order);
    }

}
