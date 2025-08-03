package com.loopers.domain.activity;

import com.loopers.annotation.ReadOnlyTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final LikedProductRepository likedProductRepository;
    private final ViewedProductRepository viewedProductRepository;

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
        Long userId = command.getUserId();
        Long productId = command.getProductId();

        likedProductRepository.findOne(userId, productId)
                .orElseGet(() -> {
                    LikedProduct likedProduct = LikedProduct.builder()
                            .userId(userId)
                            .productId(productId)
                            .build();

                    return likedProductRepository.save(likedProduct);
                });
    }

    @Transactional
    public void dislike(ActivityCommand.Dislike command) {
        likedProductRepository.findOne(command.getUserId(), command.getProductId())
                .ifPresent(likedProductRepository::delete);
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
