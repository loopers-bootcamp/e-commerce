package com.loopers.interfaces.listener.product;

import com.loopers.domain.product.ProductResult;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.event.ProductEvent;
import com.loopers.domain.product.event.ProductEventPublisher;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductService productService;
    private final ProductEventPublisher productEventPublisher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProductEvent.LikeChanged event) {
        productEventPublisher.publishEvent(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProductEvent.StockChanged event) {
        ProductResult.GetProductOptions options = productService.getProductOptions(List.of(event.productOptionId()))
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));
        Long productId = options.getItems().getFirst().getProductId();

        productEventPublisher.publishEvent(event.withProductId(productId));
    }

}
