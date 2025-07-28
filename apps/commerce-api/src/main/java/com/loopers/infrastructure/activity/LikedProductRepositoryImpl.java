package com.loopers.infrastructure.activity;

import com.loopers.domain.activity.LikedProduct;
import com.loopers.domain.activity.LikedProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LikedProductRepositoryImpl implements LikedProductRepository {

    private final LikedProductJpaRepository likedProductJpaRepository;

    @Override
    public List<LikedProduct> findLikedProductsByUserId(Long userId) {
        return likedProductJpaRepository.findByUserId(userId);
    }

    @Override
    public long countLikedProductsByProductId(Long productId) {
        return likedProductJpaRepository.countByProductId(productId);
    }

    @Override
    public boolean existsLikedProductsByUserId(Long userId) {
        return likedProductJpaRepository.existsByUserId(userId);
    }

    @Override
    public LikedProduct saveLikedProduct(LikedProduct likedProduct) {
        return likedProductJpaRepository.save(likedProduct);
    }

}
