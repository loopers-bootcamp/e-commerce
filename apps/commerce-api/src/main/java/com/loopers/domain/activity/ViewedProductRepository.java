package com.loopers.domain.activity;

import java.util.Optional;

public interface ViewedProductRepository {

    Optional<ViewedProduct> findViewedProduct(Long userId, Long productId);

    ViewedProduct saveViewedProduct(ViewedProduct viewedProduct);

}
