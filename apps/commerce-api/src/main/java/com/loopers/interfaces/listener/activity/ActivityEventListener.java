package com.loopers.interfaces.listener.activity;

import com.loopers.domain.activity.ActivityCommand;
import com.loopers.domain.activity.ActivityService;
import com.loopers.domain.activity.event.ActivityEvent;
import com.loopers.domain.product.ProductService;
import com.loopers.support.annotation.Inboxing;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ActivityEventListener {

    private final ProductService productService;
    private final ActivityService activityService;

    /**
     * {@link Async}: 기술적 이슈 + 좋아요와 함께 원자적 연산의 대상이라고 생각하지 않음.
     * <p>
     * {@link TransactionPhase#AFTER_COMMIT}: 좋아요가 성공해야 좋아요 수를 집계.
     */
    @Async
    @Inboxing(idempotent = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void likeProduct(ActivityEvent.Like event) {
        productService.like(event.productId());
    }

    /**
     * {@link Async}: 기술적 이슈 + 좋아요 취소와 함께 원자적 연산의 대상이라고 생각하지 않음.
     * <p>
     * {@link TransactionPhase#AFTER_COMMIT}: 좋아요 취소가 성공해야 좋아요 수를 집계.
     */
    @Async
    @Inboxing(idempotent = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void dislikeProduct(ActivityEvent.Dislike event) {
        productService.dislike(event.productId());
    }

    /**
     * {@link Async}: 상품 조회 시 조회수 증가를 동기적으로 기다려야 할 필요 없음.
     * <p>
     * {@link EventListener}: 상품 조회는 트랜잭션과 상관이 없다.
     */
    @Async
    @Inboxing(idempotent = true)
    @EventListener
    public void viewProduct(ActivityEvent.View event) {
        ActivityCommand.View activityCommand = ActivityCommand.View.builder()
                .userId(event.userId())
                .productId(event.productId())
                .build();
        activityService.view(activityCommand);
    }

}
