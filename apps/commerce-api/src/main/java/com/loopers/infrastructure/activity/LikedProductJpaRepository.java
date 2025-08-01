package com.loopers.infrastructure.activity;

import com.loopers.domain.activity.LikedProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikedProductJpaRepository extends JpaRepository<LikedProduct, Long> {

    List<LikedProduct> findByUserId(Long userId);

    long countByProductId(Long productId);

    Optional<LikedProduct> findByUserIdAndProductId(Long userId, Long productId);

}
