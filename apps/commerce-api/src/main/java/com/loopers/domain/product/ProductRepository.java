package com.loopers.domain.product;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Page<ProductQueryResult.Products> searchProducts(ProductQueryCommand.SearchProducts command);

    Optional<ProductQueryResult.ProductDetail> findDetail(Long productId);

    Optional<ProductQueryResult.ProductOptions> findOptions(List<Long> productOptionIds);

    Optional<Product> findProductForUpdate(Long productId);

    List<ProductStock> findStocksForUpdate(List<Long> productOptionIds);

    Product saveProduct(Product product);

    List<ProductStock> saveStocks(List<ProductStock> stocks);

}
