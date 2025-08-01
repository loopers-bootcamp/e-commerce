package com.loopers.infrastructure.activity;

import com.loopers.domain.activity.ViewedProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ViewedProductJpaRepository extends JpaRepository<ViewedProduct, Long> {

    Optional<ViewedProduct> findByUserIdAndProductId(Long userId, Long productId);

}
