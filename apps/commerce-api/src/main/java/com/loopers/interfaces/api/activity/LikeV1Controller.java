package com.loopers.interfaces.api.activity;

import com.loopers.application.activity.ActivityFacade;
import com.loopers.application.activity.ActivityInput;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/like")
public class LikeV1Controller implements LikeV1ApiSpec {

    private final ActivityFacade activityFacade;

    @PostMapping("/products/{productId}")
    @Override
    public ApiResponse<Boolean> like(
            @RequestHeader("X-USER-ID")
            String userName,
            @PathVariable @Positive
            Long productId
    ) {
        ActivityInput.Like input = ActivityInput.Like.builder()
                .userName(userName)
                .productId(productId)
                .build();

        activityFacade.like(input);

        return ApiResponse.success(true);
    }

    @DeleteMapping("/products/{productId}")
    @Override
    public ApiResponse<Boolean> dislike(
            @RequestHeader("X-USER-ID")
            String userName,
            @PathVariable @Positive
            Long productId
    ) {
        ActivityInput.Dislike input = ActivityInput.Dislike.builder()
                .userName(userName)
                .productId(productId)
                .build();

        activityFacade.dislike(input);

        return ApiResponse.success(true);
    }

}
