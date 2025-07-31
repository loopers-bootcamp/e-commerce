package com.loopers.domain.product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Optional<ProductQueryResult.ProductDetail> findProductDetailById(Long productId);

    Optional<ProductQueryResult.ProductOptions> findProductOptionsByIds(List<Long> productOptionIds);

    List<Stock> findStocksByProductOptionIdsForUpdate(List<Long> productOptionIds);

    List<Stock> saveStocks(List<Stock> stocks);

}
