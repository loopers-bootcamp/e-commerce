package com.loopers.infrastructure.activity;

import com.loopers.domain.activity.LikedProduct;
import com.loopers.domain.activity.LikedProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LikedProductRepositoryImpl implements LikedProductRepository {

    private final LikedProductJpaRepository likedProductJpaRepository;

    @Override
    public List<LikedProduct> findLikedProductsByUserId(Long userId) {
        return likedProductJpaRepository.findByUserId(userId);
    }

    @Override
    public long countByProductId(Long productId) {
        return likedProductJpaRepository.countByProductId(productId);
    }

    @Override
    public Optional<LikedProduct> findOne(Long userId, Long productId) {
        return likedProductJpaRepository.findByUserIdAndProductId(userId, productId);
    }

    @Override
    public LikedProduct save(LikedProduct likedProduct) {
        return likedProductJpaRepository.save(likedProduct);
    }

    @Override
    public void delete(LikedProduct likedProduct) {
        likedProductJpaRepository.delete(likedProduct);
    }

}
