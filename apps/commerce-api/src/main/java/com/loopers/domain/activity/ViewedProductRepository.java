package com.loopers.domain.activity;

import java.util.Optional;

public interface ViewedProductRepository {

    Optional<ViewedProduct> findOne(Long userId, Long productId);

    ViewedProduct save(ViewedProduct viewedProduct);

}
