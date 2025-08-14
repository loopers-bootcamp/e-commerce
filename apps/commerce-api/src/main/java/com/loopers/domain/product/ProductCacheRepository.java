package com.loopers.domain.product;

import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductCacheRepository {

    Page<Long> searchProductIds(ProductQueryCommand.SearchProducts command);

    List<ProductQueryResult.Products> findProducts(List<Long> productIds);

    void saveProduct(Product product);

}
