package com.loopers.domain.product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Optional<ProductQueryResult.ProductDetail> findProductDetailById(Long productId);

    Optional<Product> findProductById(Long productId);

    List<Product> findProductsByOptionIds(List<Long> optionIds);

    List<Stock> findStocksByOptionIds(List<Long> optionIds);

    List<Stock> saveStocks(List<Stock> stocks);

}
