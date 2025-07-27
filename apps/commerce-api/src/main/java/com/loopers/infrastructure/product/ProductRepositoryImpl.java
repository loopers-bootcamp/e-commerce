package com.loopers.infrastructure.product;

import com.loopers.domain.product.*;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Product> findProductByProductId(Long productId) {
        return productRepository.findById(productId);
    }

    @Override
    public Optional<Product> findProductAndOptionsByProductId(Long productId) {
        QProduct product = QProduct.product;
        QProductOption option = QProductOption.productOption;

        List<Tuple> rows = queryFactory
                .select(product, option)
                .from(product)
                .leftJoin(option).on(option.productId.eq(product.id))
                .where(product.id.eq(productId))
                .fetch();

        if (CollectionUtils.isEmpty(rows)) {
            return Optional.empty();
        }

        Product prd = rows.getFirst().get(product);
        List<ProductOption> opts = rows.stream()
                .map(row -> row.get(option))
                .filter(Objects::nonNull)
                .toList();
        prd.addOptions(opts);

        return Optional.of(prd);
    }

    @Override
    public List<Product> findProductsByOptionIds(List<Long> optionIds) {
        return List.of();
    }

    @Override
    public List<Stock> findStocksByOptionIds(List<Long> optionIds) {
        return List.of();
    }

    @Override
    public List<Stock> saveStocks(List<Stock> stocks) {
        return List.of();
    }

}
