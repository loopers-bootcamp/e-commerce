package com.loopers.domain.product;

import org.springframework.data.domain.Page;

import java.util.Optional;

public interface ProductCacheRepository {

    Page<ProductQueryResult.Products> searchProducts(ProductQueryCommand.SearchProducts command);

    void saveProducts(
            ProductQueryCommand.SearchProducts command,
            Page<ProductQueryResult.Products> page
    );

    Optional<ProductQueryResult.ProductDetail> findProductDetailById(Long productId);

    void saveProductDetail(
            Long productId,
            ProductQueryResult.ProductDetail productDetail
    );

}
