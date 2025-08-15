package com.loopers.domain.product;

import org.springframework.data.domain.Page;

public interface ProductCacheRepository {

    Page<ProductQueryResult.Products> searchProducts(ProductQueryCommand.SearchProducts command);

    void saveProducts(
            ProductQueryCommand.SearchProducts command,
            Page<ProductQueryResult.Products> page
    );

}
