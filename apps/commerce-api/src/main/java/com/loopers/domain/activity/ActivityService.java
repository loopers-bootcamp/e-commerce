package com.loopers.domain.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final LikedProductRepository likedProductRepository;
    private final ViewedProductRepository viewedProductRepository;

    @Transactional
    public void like(ActivityCommand.Like command) {
        LikedProduct likedProduct = LikedProduct.builder()
                .userId(command.getUserId())
                .productId(command.getProductId())
                .build();

        likedProductRepository.saveLikedProduct(likedProduct);
    }

    @Transactional
    public long view(ActivityCommand.View command) {
        Long userId = command.getUserId();
        Long productId = command.getProductId();

        ViewedProduct viewedProduct = viewedProductRepository.findViewedProduct(userId, productId)
                .orElseGet(() -> ViewedProduct.builder()
                        .viewedCount(0L)
                        .userId(userId)
                        .productId(productId)
                        .build());

        viewedProduct.view();

        viewedProductRepository.saveViewedProduct(viewedProduct);

        return viewedProduct.getViewCount();
    }

}
