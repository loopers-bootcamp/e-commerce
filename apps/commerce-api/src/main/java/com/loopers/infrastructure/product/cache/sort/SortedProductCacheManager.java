package com.loopers.infrastructure.product.cache.sort;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.attribute.ProductSearchSortType;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SortedProductCacheManager {

    int MAX_MEMBER_SIZE = 500;
    String KEY_PREFIX = "page:products:";

    boolean supports(ProductSearchSortType sortType);

    Page<Long> getProductIds(@Nullable Long brandId, Pageable pageable);

    void saveProduct(Product product);

}
