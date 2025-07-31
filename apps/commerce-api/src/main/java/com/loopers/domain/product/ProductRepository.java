package com.loopers.domain.product;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Page<Product> searchProducts(ProductQueryCommand.SearchProducts queryCommand);

    Optional<ProductQueryResult.ProductDetail> findProductDetailById(Long productId);

    Optional<ProductQueryResult.ProductOptions> findProductOptionsByIds(List<Long> productOptionIds);

    List<Stock> findStocksByProductOptionIdsForUpdate(List<Long> productOptionIds);

    List<Stock> saveStocks(List<Stock> stocks);

}
