package com.loopers.interfaces.listener.product;

import com.loopers.domain.product.event.ProductEvent;
import com.loopers.domain.product.event.ProductEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductEventPublisher productEventPublisher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProductEvent.LikeChanged event) {
        productEventPublisher.publishEvent(event);
    }

}
