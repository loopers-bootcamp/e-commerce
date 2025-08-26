package com.loopers.application.activity;

import com.loopers.domain.activity.event.ActivityEvent;
import com.loopers.domain.product.ProductService;
import com.loopers.support.annotation.Inboxing;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ActivityEventListener {

    private final ProductService productService;

    @Async
    @Inboxing
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void likeProduct(ActivityEvent.Like event) {
        productService.like(event.productId());
    }

    @Async
    @Inboxing
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void dislikeProduct(ActivityEvent.Dislike event) {
        productService.dislike(event.productId());
    }

}
