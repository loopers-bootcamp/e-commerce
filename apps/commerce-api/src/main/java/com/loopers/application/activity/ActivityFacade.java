package com.loopers.application.activity;

import com.loopers.domain.activity.ActivityCommand;
import com.loopers.domain.activity.ActivityResult;
import com.loopers.domain.activity.ActivityService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserResult;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityFacade {

    private final ActivityService activityService;
    private final UserService userService;
    private final ProductService productService;

    public ActivityOutput.GetLikedProducts getLikedProducts(String userName) {
        UserResult.GetUser user = userService.getUser(userName)
                .orElseThrow(() -> new BusinessException(CommonErrorType.UNAUTHENTICATED));

        ActivityResult.GetLikedProducts likedProducts = activityService.getLikedProducts(user.getUserId());

        return ActivityOutput.GetLikedProducts.from(likedProducts);
    }

    @CacheEvict(cacheNames = "detail:product", key = "#input.productId")
    @Transactional
    public void like(ActivityInput.Like input) {
        UserResult.GetUser user = userService.getUser(input.getUserName())
                .orElseThrow(() -> new BusinessException(CommonErrorType.UNAUTHENTICATED));

        Long productId = input.getProductId();
        productService.like(productId);

        ActivityCommand.Like command = ActivityCommand.Like.builder()
                .userId(user.getUserId())
                .productId(productId)
                .build();
        activityService.like(command);
    }

    @CacheEvict(cacheNames = "detail:product", key = "#input.productId")
    @Transactional
    public void dislike(ActivityInput.Dislike input) {
        UserResult.GetUser user = userService.getUser(input.getUserName())
                .orElseThrow(() -> new BusinessException(CommonErrorType.UNAUTHENTICATED));

        Long productId = input.getProductId();
        productService.dislike(productId);

        ActivityCommand.Dislike command = ActivityCommand.Dislike.builder()
                .userId(user.getUserId())
                .productId(productId)
                .build();
        activityService.dislike(command);
    }

}
