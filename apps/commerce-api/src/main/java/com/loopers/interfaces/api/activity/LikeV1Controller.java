package com.loopers.interfaces.api.activity;

import com.loopers.application.activity.ActivityFacade;
import com.loopers.application.activity.ActivityInput;
import com.loopers.application.activity.ActivityOutput;
import com.loopers.interfaces.api.ApiHeader;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/like")
public class LikeV1Controller implements LikeV1ApiSpec {

    private final ActivityFacade activityFacade;

    @GetMapping("/products")
    @Override
    public ApiResponse<LikeResponse.GetLikedProducts> getLikedProducts(
            @RequestHeader(ApiHeader.USER_ID)
            String userName
    ) {
        ActivityOutput.GetLikedProducts likedProducts = activityFacade.getLikedProducts(userName);
        LikeResponse.GetLikedProducts response = LikeResponse.GetLikedProducts.from(likedProducts);

        return ApiResponse.success(response);
    }

    @PostMapping("/products/{productId}")
    @Override
    public ApiResponse<Boolean> like(
            @RequestHeader(ApiHeader.USER_ID)
            String userName,
            @PathVariable
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
            @RequestHeader(ApiHeader.USER_ID)
            String userName,
            @PathVariable
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
