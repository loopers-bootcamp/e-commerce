package com.loopers.domain.activity;

import com.loopers.annotation.ReadOnlyTransactional;
import com.loopers.domain.activity.event.ActivityEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final LikedProductRepository likedProductRepository;
    private final ViewedProductRepository viewedProductRepository;
    private final ApplicationEventPublisher eventPublisher;

    @ReadOnlyTransactional
    public ActivityResult.GetLikedProducts getLikedProducts(Long userId) {
        List<ActivityQueryResult.GetLikedProducts> products = likedProductRepository.findByUserId(userId);
        return ActivityResult.GetLikedProducts.from(products);
    }

    @ReadOnlyTransactional
    public long getLikeCount(Long productId) {
        return likedProductRepository.countByProductId(productId);
    }

    @Transactional
    public void like(ActivityCommand.Like command) {
        LikedProduct likedProduct = LikedProduct.builder()
                .userId(command.getUserId())
                .productId(command.getProductId())
                .build();

        boolean saved = likedProductRepository.saveIfAbsent(likedProduct);

        if (saved) {
            eventPublisher.publishEvent(ActivityEvent.Like.from(likedProduct));
        }
    }

    @Transactional
    public void dislike(ActivityCommand.Dislike command) {
        LikedProduct likedProduct = LikedProduct.builder()
                .userId(command.getUserId())
                .productId(command.getProductId())
                .build();

        boolean deleted = likedProductRepository.deleteIfPresent(likedProduct);

        if (deleted) {
            eventPublisher.publishEvent(ActivityEvent.Dislike.from(likedProduct));
        }
    }

    @Transactional
    public long view(ActivityCommand.View command) {
        Long userId = command.getUserId();
        Long productId = command.getProductId();

        ViewedProduct viewedProduct = viewedProductRepository.findOne(userId, productId)
                .orElseGet(() -> ViewedProduct.builder()
                        .viewCount(0L)
                        .userId(userId)
                        .productId(productId)
                        .build());

        viewedProduct.view();

        viewedProductRepository.save(viewedProduct);

        return viewedProduct.getViewCount();
    }

}
