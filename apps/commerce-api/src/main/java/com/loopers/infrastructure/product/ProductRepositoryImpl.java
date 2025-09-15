package com.loopers.infrastructure.product;

import com.loopers.domain.brand.QBrand;
import com.loopers.domain.product.*;
import com.loopers.domain.product.attribute.ProductSearchSortType;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
    public Page<ProductQueryResult.Products> searchProducts(ProductQueryCommand.SearchProducts command) {
        PageRequest pageRequest = PageRequest.of(command.getPage(), command.getSize());

        QProduct p = QProduct.product;
        QBrand b = QBrand.brand;

        String keyword = command.getKeyword();
        Long brandId = command.getBrandId();
        ProductSearchSortType sortType = command.getSort();

        JPAQuery<Tuple> query = queryFactory
                .select(
                        p.id
                        , p.name
                        , p.basePrice
                        , p.likeCount
                        , p.brandId
                        , b.name
                )
                .from(p)
                .leftJoin(b).on(b.id.eq(p.brandId))
                .where(
                        containKeywordByProductName(keyword),
                        matchByBrandId(brandId)
                )
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .orderBy(productsSorter(sortType), tieBreakSorter());

        JPAQuery<Long> countQuery = queryFactory
                .select(p.count())
                .from(p)
                .leftJoin(b).on(b.id.eq(p.brandId))
                .where(
                        containKeywordByProductName(keyword),
                        matchByBrandId(brandId)
                );

        List<ProductQueryResult.Products> products = query.stream()
                .map(row -> new ProductQueryResult.Products(
                        row.get(p.id),
                        row.get(p.name),
                        row.get(p.basePrice),
                        row.get(p.likeCount),
                        row.get(p.brandId),
                        row.get(b.name)
                ))
                .toList();

        return PageableExecutionUtils.getPage(products, pageRequest, countQuery::fetchOne);
    }

    @Override
    public Optional<ProductQueryResult.ProductDetail> findDetail(Long productId) {
        QBrand b = QBrand.brand;
        QProduct p = QProduct.product;
        QProductOption po = QProductOption.productOption;
        QProductStock ps = QProductStock.productStock;

        List<Tuple> rows = queryFactory
                .select(
                        p.id
                        , p.name
                        , p.basePrice
                        , p.likeCount
                        , p.brandId
                        , b.name
                        , po.id
                        , po.name
                        , po.additionalPrice
                        , po.productId
                        , ps.quantity
                )
                .from(p)
                .leftJoin(b).on(b.id.eq(p.brandId))
                .leftJoin(po).on(po.productId.eq(p.id))
                .leftJoin(ps).on(ps.productOptionId.eq(po.id))
                .where(p.id.eq(productId))
                .fetch();

        if (CollectionUtils.isEmpty(rows)) {
            return Optional.empty();
        }

        ProductQueryResult.ProductDetail detail = new ProductQueryResult.ProductDetail(
                rows.getFirst().get(p.id),
                rows.getFirst().get(p.name),
                rows.getFirst().get(p.basePrice),
                rows.getFirst().get(p.likeCount),
                rows.getFirst().get(p.brandId),
                rows.getFirst().get(b.name),
                new ArrayList<>()
        );

        for (Tuple row : rows) {
            if (row.get(po.id) == null) {
                continue;
            }

            ProductQueryResult.ProductDetail.Option item = new ProductQueryResult.ProductDetail.Option(
                    row.get(po.id),
                    row.get(po.name),
                    row.get(po.additionalPrice),
                    row.get(po.productId),
                    row.get(ps.quantity)
            );

            detail.options().add(item);
        }

        return Optional.of(detail);
    }

    @Override
    public Optional<ProductQueryResult.ProductOptions> findOptions(List<Long> productOptionIds) {
        QProduct p = QProduct.product;
        QProductOption po = QProductOption.productOption;
        QProductStock ps = QProductStock.productStock;

        NumberExpression<Integer> salePrice = p.basePrice.add(po.additionalPrice).as("salePrice");

        List<ProductQueryResult.ProductOptions.Item> items = queryFactory
                .select(
                        po.id,
                        salePrice,
                        ps.quantity,
                        p.id
                )
                .from(po)
                .join(p).on(p.id.eq(po.productId))
                .join(ps).on(ps.productOptionId.eq(po.id))
                .where(po.id.in(productOptionIds))
                .stream()
                .map(row -> new ProductQueryResult.ProductOptions.Item(
                                row.get(po.id),
                                row.get(salePrice),
                                row.get(ps.quantity),
                                row.get(p.id)
                        )
                )
                .toList();

        if (CollectionUtils.isEmpty(items)) {
            return Optional.empty();
        }

        ProductQueryResult.ProductOptions queryResult = new ProductQueryResult.ProductOptions(items);
        return Optional.of(queryResult);
    }

    @Override
    public Optional<Product> findProductForUpdate(Long productId) {
        return productRepository.findByIdForUpdate(productId);
    }

    @Override
    public List<ProductStock> findStocksForUpdate(List<Long> productOptionIds) {
        return stockJpaRepository.findByProductOptionIdIn(productOptionIds);
    }

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public List<ProductStock> saveStocks(List<ProductStock> stocks) {
        return stockJpaRepository.saveAll(stocks);
    }

    // -------------------------------------------------------------------------------------------------

    private static BooleanExpression containKeywordByProductName(String keyword) {
        QProduct p = QProduct.product;
        return StringUtils.hasText(keyword) ? p.name.containsIgnoreCase(keyword) : null;
    }

    private static BooleanExpression matchByBrandId(Long brandId) {
        QBrand b = QBrand.brand;
        return brandId == null ? null : b.id.eq(brandId);
    }

    private static OrderSpecifier<? extends Comparable<?>> productsSorter(ProductSearchSortType sortType) {
        QProduct p = QProduct.product;

        return switch (sortType) {
            case LATEST -> p.createdAt.desc();
            case POPULAR -> p.likeCount.desc();
            case CHEAP -> p.basePrice.asc();
        };
    }

    private static OrderSpecifier<Long> tieBreakSorter() {
        QProduct p = QProduct.product;
        return p.id.desc();
    }

}
