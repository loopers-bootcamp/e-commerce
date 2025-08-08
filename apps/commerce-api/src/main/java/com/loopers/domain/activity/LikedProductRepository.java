package com.loopers.domain.activity;

import java.util.List;
import java.util.Optional;

public interface LikedProductRepository {

    List<ActivityQueryResult.GetLikedProducts> findByUserId(Long userId);

    long countByProductId(Long productId);

    Optional<LikedProduct> findOne(Long userId, Long productId);

    LikedProduct save(LikedProduct likedProduct);

    void saveIfAbsent(LikedProduct likedProduct);

    void deleteIfPresent(LikedProduct likedProduct);

}
