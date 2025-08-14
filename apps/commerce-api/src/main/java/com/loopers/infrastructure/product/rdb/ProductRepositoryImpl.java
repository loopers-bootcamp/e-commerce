package com.loopers.infrastructure.product.rdb;

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
        List<Long> productIds = command.getProductIds();
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
                        matchByBrandId(brandId),
                        matchByProductIds(productIds)
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
                        matchByBrandId(brandId),
                        matchByProductIds(productIds)
                );

        List<ProductQueryResult.Products> products = query.stream()
                .map(row -> ProductQueryResult.Products.builder()
                        .productId(row.get(p.id))
                        .productName(row.get(p.name))
                        .basePrice(row.get(p.basePrice))
                        .likeCount(row.get(p.likeCount))
                        .brandId(row.get(p.brandId))
                        .brandName(row.get(b.name))
                        .build()
                )
                .toList();

        return PageableExecutionUtils.getPage(products, pageRequest, countQuery::fetchOne);
    }

    @Override
    public Optional<ProductQueryResult.ProductDetail> findProductDetailById(Long productId) {
        QProduct p = QProduct.product;
        QProductOption po = QProductOption.productOption;
        QProductStock ps = QProductStock.productStock;

        List<Tuple> rows = queryFactory
                .select(p, po, ps)
                .from(p)
                .leftJoin(po).on(po.productId.eq(p.id))
                .leftJoin(ps).on(ps.productOptionId.eq(po.id))
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
                .likeCount(product.getLikeCount())
                .brandId(product.getBrandId())
                .options(new ArrayList<>())
                .build();

        for (Tuple row : rows) {
            ProductOption option = row.get(po);
            if (option == null) {
                continue;
            }

            ProductStock stock = row.get(ps);
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
                .map(row ->
                        ProductQueryResult.ProductOptions.Item.builder()
                                .productOptionId(row.get(po.id))
                                .salePrice(row.get(salePrice))
                                .stockQuantity(row.get(ps.quantity))
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

    private static BooleanExpression matchByProductIds(List<Long> productIds) {
        QProduct p = QProduct.product;
        return CollectionUtils.isEmpty(productIds) ? null : p.id.in(productIds);
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
