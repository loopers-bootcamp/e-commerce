package com.loopers.infrastructure.product;

import com.loopers.domain.product.*;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.loopers.domain.activity.QLikedProduct.likedProduct;
import static com.loopers.domain.brand.QBrand.brand;
import static com.loopers.domain.product.QProduct.product;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productRepository;
    private final ProductOptionJpaRepository productOptionRepository;
    private final StockJpaRepository stockJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> searchProducts(ProductQueryCommand.SearchProducts queryCommand) {
        PageRequest pageable = PageRequest.of(queryCommand.getPage(), queryCommand.getSize());

        String keyword = queryCommand.getKeyword();
        Long brandId = queryCommand.getBrandId();

        Predicate containKeywordByProductName = StringUtils.hasText(keyword) ? product.name.containsIgnoreCase(keyword) : null;
        Predicate matchByBrand = brandId == null ? null : brand.id.eq(brandId);

        OrderSpecifier<? extends Serializable> orderSpecifier = switch (queryCommand.getSortType()) {
            case LATEST -> product.createdAt.desc();
            case POPULAR -> new OrderSpecifier<>(
                    Order.DESC,
                    JPAExpressions
                            .select(likedProduct.id.count())
                            .from(likedProduct)
                            .where(likedProduct.productId.eq(product.id)),
                    OrderSpecifier.NullHandling.NullsLast
            );
            case CHEAP -> product.basePrice.asc();
        };

        JPAQuery<Product> query = queryFactory
                .select(product)
                .from(product)
                .leftJoin(brand).on(brand.id.eq(product.brandId))
                .where(
                        containKeywordByProductName,
                        matchByBrand
                )
                .groupBy(product.id)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifier, product.id.desc());

        List<Product> products = query.fetch();
        Long total = queryFactory
                .select(product.count())
                .from(product)
                .leftJoin(brand).on(brand.id.eq(product.brandId))
                .where(
                        containKeywordByProductName,
                        matchByBrand
                )
                .fetchOne();

        return new PageImpl<>(products, pageable, total);
    }

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
    public Optional<ProductQueryResult.ProductOptions> findProductOptionsByIds(List<Long> productOptionIds) {
        QProduct p = QProduct.product;
        QProductOption po = QProductOption.productOption;
        QStock s = QStock.stock;

        NumberExpression<Integer> salePrice = p.basePrice.add(po.additionalPrice).as("salePrice");

        List<ProductQueryResult.ProductOptions.Item> items = queryFactory
                .select(
                        po.id,
                        salePrice,
                        s.quantity,
                        p.id
                )
                .from(po)
                .join(p).on(p.id.eq(po.productId))
                .join(s).on(s.productOptionId.eq(po.id))
                .where(po.id.in(productOptionIds))
                .stream()
                .map(row ->
                        ProductQueryResult.ProductOptions.Item.builder()
                                .productOptionId(row.get(po.id))
                                .salePrice(row.get(salePrice))
                                .stockQuantity(row.get(s.quantity))
                                .productId(row.get(p.id))
                                .build()
                )
                .toList();

        if (CollectionUtils.isEmpty(items)) {
            return Optional.empty();
        }

        ProductQueryResult.ProductOptions queryResult = ProductQueryResult.ProductOptions.builder()
                .items(items)
                .build();

        return Optional.of(queryResult);
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
