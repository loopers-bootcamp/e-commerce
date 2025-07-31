package com.loopers.domain.activity;

import java.util.List;
import java.util.Optional;

public interface LikedProductRepository {

    List<LikedProduct> findLikedProductsByUserId(Long userId);

    long countLikedProductsByProductId(Long productId);

    Optional<LikedProduct> findOne(Long userId, Long productId);

    LikedProduct save(LikedProduct likedProduct);

    void delete(LikedProduct likedProduct);

}
