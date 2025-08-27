package com.loopers.application.activity;

import com.loopers.domain.activity.ActivityCommand;
import com.loopers.domain.activity.ActivityService;
import com.loopers.domain.activity.event.ActivityEvent;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserResult;
import com.loopers.domain.user.UserService;
import com.loopers.support.annotation.Inboxing;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
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
    private final UserService userService;
    private final ActivityService activityService;

    @Async
    @Inboxing(idempotent = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void likeProduct(ActivityEvent.Like event) {
        productService.like(event.productId());
    }

    @Async
    @Inboxing(idempotent = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void dislikeProduct(ActivityEvent.Dislike event) {
        productService.dislike(event.productId());
    }

    @Async
    @Inboxing(idempotent = true)
    @EventListener
    public void viewProduct(ActivityEvent.View event) {
        UserResult.GetUser user = userService.getUser(event.userName())
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));

        ActivityCommand.View activityCommand = ActivityCommand.View.builder()
                .userId(user.getUserId())
                .productId(event.productId())
                .build();
        activityService.view(activityCommand);
    }

}
