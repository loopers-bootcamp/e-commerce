package com.loopers.infrastructure.activity;

import com.loopers.domain.activity.LikedProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LikedProductJpaRepository extends JpaRepository<LikedProduct, Long> {

    List<LikedProduct> findByUserId(Long userId);

    long countByProductId(Long productId);

    boolean existsByUserId(Long userId);

}
