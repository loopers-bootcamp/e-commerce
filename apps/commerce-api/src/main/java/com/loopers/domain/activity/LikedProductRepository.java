package com.loopers.domain.activity;

import java.util.List;

public interface LikedProductRepository {

    List<LikedProduct> findLikedProductsByUserId(Long userId);

    long countLikedProductsByProductId(Long productId);

    boolean existsLikedProductsByUserId(Long userId);

    LikedProduct saveLikedProduct(LikedProduct likedProduct);

}
