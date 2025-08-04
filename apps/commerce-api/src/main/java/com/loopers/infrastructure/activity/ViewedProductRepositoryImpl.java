package com.loopers.infrastructure.activity;

import com.loopers.domain.activity.ViewedProduct;
import com.loopers.domain.activity.ViewedProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ViewedProductRepositoryImpl implements ViewedProductRepository {

    private final ViewedProductJpaRepository viewedProductJpaRepository;

    @Override
    public Optional<ViewedProduct> findOne(Long userId, Long productId) {
        return viewedProductJpaRepository.findByUserIdAndProductId(userId, productId);
    }

    @Override
    public ViewedProduct save(ViewedProduct viewedProduct) {
        return viewedProductJpaRepository.save(viewedProduct);
    }

}
