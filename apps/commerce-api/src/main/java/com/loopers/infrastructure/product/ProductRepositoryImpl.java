package com.loopers.infrastructure.product;

import com.loopers.domain.product.*;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productRepository;
    private final ProductOptionJpaRepository productOptionRepository;
    private final StockJpaRepository stockJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ProductQueryResult.ProductDetail> findProductDetailById(Long productId) {
        QProduct p = QProduct.product;
        QProductOption po = QProductOption.productOption;
        QStock s = QStock.stock;

        List<Tuple> rows = queryFactory
                .select(p, po, s)
                .from(p)
                .leftJoin(po).on(po.productId.eq(p.id))
                .leftJoin(s).on(s.productOptionId.eq(po.id))
                .where(p.id.eq(productId))
                .fetch();

        if (CollectionUtils.isEmpty(rows)) {
            return Optional.empty();
        }

        Product product = rows.getFirst().get(p);
        ProductQueryResult.ProductDetail detail = ProductQueryResult.ProductDetail.builder()
                .productId(product.getId())
                .productName(product.getName())
                .basePrice(product.getBasePrice())
                .brandId(product.getBrandId())
                .options(new ArrayList<>())
                .build();

        for (Tuple row : rows) {
            ProductOption option = row.get(po);
            if (option == null) {
                continue;
            }

            Stock stock = row.get(s);
            ProductQueryResult.ProductDetail.Option item = ProductQueryResult.ProductDetail.Option.builder()
                    .productOptionId(option.getId())
                    .productOptionName(option.getName())
                    .additionalPrice(option.getAdditionalPrice())
                    .productId(option.getProductId())
                    .stockQuantity(stock.getQuantity())
                    .build();

            detail.getOptions().add(item);
        }

        return Optional.of(detail);
    }

    @Override
    public Optional<Product> findProductById(Long productId) {
        return productRepository.findById(productId);
    }

    @Override
    public List<Stock> findStocksByProductOptionIdsForUpdate(List<Long> productOptionIds) {
        return stockJpaRepository.findByProductOptionIdIn(productOptionIds);
    }

    @Override
    public List<Stock> saveStocks(List<Stock> stocks) {
        return stockJpaRepository.saveAll(stocks);
    }

}
