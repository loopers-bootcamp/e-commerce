package com.loopers.application.activity;

import com.loopers.domain.activity.ActivityCommand;
import com.loopers.domain.activity.ActivityService;
import com.loopers.domain.user.UserResult;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityFacade {

    private final ActivityService activityService;
    private final UserService userService;

    public void like(ActivityInput.Like input) {
        UserResult.GetUser user = userService.getUser(input.getUserName())
                .orElseThrow(() -> new BusinessException(CommonErrorType.UNAUTHENTICATED));

        ActivityCommand.Like command = ActivityCommand.Like.builder()
                .userId(user.getUserId())
                .productId(input.getProductId())
                .build();
        activityService.like(command);
    }

    public void dislike(ActivityInput.Dislike input) {
        UserResult.GetUser user = userService.getUser(input.getUserName())
                .orElseThrow(() -> new BusinessException(CommonErrorType.UNAUTHENTICATED));

        ActivityCommand.Dislike command = ActivityCommand.Dislike.builder()
                .userId(user.getUserId())
                .productId(input.getProductId())
                .build();
        activityService.dislike(command);
    }

}
