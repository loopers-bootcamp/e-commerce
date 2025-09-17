package com.loopers.infrastructure.product;

import com.loopers.domain.product.*;
import com.loopers.domain.product.attribute.ProductSearchSortType;
import com.loopers.domain.brand.QBrand;
import com.loopers.domain.product.QProduct;
import com.loopers.domain.product.QProductOption;
import com.loopers.domain.product.QProductStock;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testing library and framework: JUnit 5 (Jupiter) + Mockito + AssertJ.
 * These tests focus on the repository implementation logic and mapping behavior,
 * with heavy mocking of QueryDSL fluent APIs to avoid DB dependencies.
 */
@ExtendWith(MockitoExtension.class)
class ProductRepositoryImplTest {

    @Mock private ProductJpaRepository productRepository;
    @Mock private ProductOptionJpaRepository productOptionRepository;
    @Mock private StockJpaRepository stockJpaRepository;
    @Mock private JPAQueryFactory queryFactory;

    // Query mocks (typed)
    @Mock private JPAQuery<Tuple> tupleQuery;
    @Mock private JPAQuery<Long> countQuery;
    @Mock private JPAQuery<Tuple> tupleQuery2; // for secondary method chains
    @Mock private JPAQuery<Tuple> tupleQuery3; // for findOptions

    @InjectMocks
    private ProductRepositoryImpl sut;

    // Q-classes used in mapping/verifications
    private final QProduct p = QProduct.product;
    private final QBrand b = QBrand.brand;
    private final QProductOption po = QProductOption.productOption;
    private final QProductStock ps = QProductStock.productStock;

    @BeforeEach
    void setup() {
        // Default stubbing for chain operations to be lenient across tests
        lenient().when(tupleQuery.from(any())).thenReturn(tupleQuery);
        lenient().when(tupleQuery.leftJoin(any(Expression.class))).thenReturn(tupleQuery);
        lenient().when(tupleQuery.leftJoin(any())).thenReturn(tupleQuery);
        lenient().when(tupleQuery.join(any())).thenReturn(tupleQuery);
        lenient().when(tupleQuery.on(any())).thenReturn(tupleQuery);
        lenient().when(tupleQuery.where(any())).thenReturn(tupleQuery);
        lenient().when(tupleQuery.where(any(), any())).thenReturn(tupleQuery);
        lenient().when(tupleQuery.offset(anyLong())).thenReturn(tupleQuery);
        lenient().when(tupleQuery.limit(anyLong())).thenReturn(tupleQuery);
        lenient().when(tupleQuery.orderBy(any(OrderSpecifier[].class))).thenReturn(tupleQuery);
        lenient().when(tupleQuery.orderBy(any(), any())).thenReturn(tupleQuery);

        lenient().when(tupleQuery2.from(any())).thenReturn(tupleQuery2);
        lenient().when(tupleQuery2.leftJoin(any())).thenReturn(tupleQuery2);
        lenient().when(tupleQuery2.join(any())).thenReturn(tupleQuery2);
        lenient().when(tupleQuery2.on(any())).thenReturn(tupleQuery2);
        lenient().when(tupleQuery2.where(any())).thenReturn(tupleQuery2);
        lenient().when(tupleQuery2.where(any(), any())).thenReturn(tupleQuery2);
        lenient().when(tupleQuery2.offset(anyLong())).thenReturn(tupleQuery2);
        lenient().when(tupleQuery2.limit(anyLong())).thenReturn(tupleQuery2);
        lenient().when(tupleQuery2.orderBy(any(OrderSpecifier[].class))).thenReturn(tupleQuery2);
        lenient().when(tupleQuery2.orderBy(any(), any())).thenReturn(tupleQuery2);

        lenient().when(tupleQuery3.from(any())).thenReturn(tupleQuery3);
        lenient().when(tupleQuery3.join(any())).thenReturn(tupleQuery3);
        lenient().when(tupleQuery3.on(any())).thenReturn(tupleQuery3);
        lenient().when(tupleQuery3.where(any())).thenReturn(tupleQuery3);

        lenient().when(countQuery.from(any())).thenReturn(countQuery);
        lenient().when(countQuery.leftJoin(any())).thenReturn(countQuery);
        lenient().when(countQuery.on(any())).thenReturn(countQuery);
        lenient().when(countQuery.where(any())).thenReturn(countQuery);
        lenient().when(countQuery.where(any(), any())).thenReturn(countQuery);
    }

    // Utility: create a mocked Tuple returning values for given expressions
    private Tuple mockTuple(Map<Expression<?>, Object> values) {
        Tuple t = mock(Tuple.class);
        values.forEach((expr, val) -> {
            // Use lenient stubbing to avoid UnnecessaryStubbingException
            lenient().when(t.get(expr)).thenReturn(val);
        });
        return t;
    }

    @Nested
    @DisplayName("searchProducts")
    class SearchProductsTests {

        private void stubSearchSelectChain(Stream<Tuple> rows, long count) {
            // select for product rows
            when(queryFactory.select(any())).thenReturn(tupleQuery); // 1st call (Tuple)
            when(tupleQuery.from(eq(p))).thenReturn(tupleQuery);
            when(tupleQuery.leftJoin(b)).thenReturn(tupleQuery);
            when(tupleQuery.on(eq(b.id.eq(p.brandId)))).thenReturn(tupleQuery);
            when(tupleQuery.where(any(), any())).thenReturn(tupleQuery);
            when(tupleQuery.offset(anyLong())).thenReturn(tupleQuery);
            when(tupleQuery.limit(anyLong())).thenReturn(tupleQuery);
            when(tupleQuery.orderBy(any(), any())).thenReturn(tupleQuery);
            when(tupleQuery.stream()).thenReturn(rows);

            // select for count
            when(queryFactory.select(eq(p.count()))).thenReturn(countQuery); // 2nd call (Long)
            when(countQuery.from(eq(p))).thenReturn(countQuery);
            when(countQuery.leftJoin(b)).thenReturn(countQuery);
            when(countQuery.on(eq(b.id.eq(p.brandId)))).thenReturn(countQuery);
            when(countQuery.where(any(), any())).thenReturn(countQuery);
            when(countQuery.fetchOne()).thenReturn(count);
        }

        @Test
        @DisplayName("returns paged products and maps tuple fields correctly")
        void returnsPagedProducts() {
            Tuple row1 = mockTuple(Map.of(
                    p.id, 101L, p.name, "Alpha Tee", p.basePrice, 1999, p.likeCount, 5, p.brandId, 77L, b.name, "BrandA"
            ));
            Tuple row2 = mockTuple(Map.of(
                    p.id, 102L, p.name, "Beta Tee", p.basePrice, 2999, p.likeCount, 9, p.brandId, 77L, b.name, "BrandA"
            ));
            stubSearchSelectChain(Stream.of(row1, row2), 2L);

            ProductQueryCommand.SearchProducts cmd = new ProductQueryCommand.SearchProducts(
                    "tee", 77L, ProductSearchSortType.POPULAR, 0, 10
            );

            Page<ProductQueryResult.Products> page = sut.searchProducts(cmd);

            assertThat(page.getTotalElements()).isEqualTo(2);
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getContent().get(0).id()).isEqualTo(101L);
            assertThat(page.getContent().get(0).name()).isEqualTo("Alpha Tee");
            assertThat(page.getContent().get(0).basePrice()).isEqualTo(1999);
            assertThat(page.getContent().get(0).likeCount()).isEqualTo(5);
            assertThat(page.getContent().get(0).brandId()).isEqualTo(77L);
            assertThat(page.getContent().get(0).brandName()).isEqualTo("BrandA");
        }

        @Test
        @DisplayName("handles empty keyword and brandId (null filters) without errors")
        void handlesNullFilters() {
            Tuple row = mockTuple(Map.of(
                    p.id, 201L, p.name, "Gamma", p.basePrice, 999, p.likeCount, 0, p.brandId, 0L, b.name, null
            ));
            stubSearchSelectChain(Stream.of(row), 1L);

            ProductQueryCommand.SearchProducts cmd = new ProductQueryCommand.SearchProducts(
                    "", null, ProductSearchSortType.LATEST, 0, 20
            );

            Page<ProductQueryResult.Products> page = sut.searchProducts(cmd);

            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getContent().get(0).name()).isEqualTo("Gamma");
        }

        @Test
        @DisplayName("applies sort: LATEST then ID desc tie-breaker")
        void appliesSortLatest() {
            stubSearchSelectChain(Stream.empty(), 0L);

            ProductQueryCommand.SearchProducts cmd = new ProductQueryCommand.SearchProducts(
                    null, null, ProductSearchSortType.LATEST, 0, 5
            );

            sut.searchProducts(cmd);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<OrderSpecifier<?>> orderCaptor = ArgumentCaptor.forClass(OrderSpecifier.class);
            verify(tupleQuery).orderBy(orderCaptor.capture(), orderCaptor.capture());
            List<OrderSpecifier<?>> specifiers = orderCaptor.getAllValues();

            assertThat(specifiers).hasSize(2);
            // First: createdAt DESC
            assertThat(specifiers.get(0).getOrder()).isEqualTo(Order.DESC);
            assertThat(specifiers.get(0).getTarget()).isEqualTo(p.createdAt);
            // Second: id DESC (tie-breaker)
            assertThat(specifiers.get(1).getOrder()).isEqualTo(Order.DESC);
            assertThat(specifiers.get(1).getTarget()).isEqualTo(p.id);
        }

        @Test
        @DisplayName("applies sort: POPULAR then ID desc tie-breaker")
        void appliesSortPopular() {
            stubSearchSelectChain(Stream.empty(), 0L);

            ProductQueryCommand.SearchProducts cmd = new ProductQueryCommand.SearchProducts(
                    null, null, ProductSearchSortType.POPULAR, 0, 5
            );

            sut.searchProducts(cmd);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<OrderSpecifier<?>> orderCaptor = ArgumentCaptor.forClass(OrderSpecifier.class);
            verify(tupleQuery).orderBy(orderCaptor.capture(), orderCaptor.capture());
            List<OrderSpecifier<?>> specifiers = orderCaptor.getAllValues();

            assertThat(specifiers.get(0).getOrder()).isEqualTo(Order.DESC);
            assertThat(specifiers.get(0).getTarget()).isEqualTo(p.likeCount);
            assertThat(specifiers.get(1).getOrder()).isEqualTo(Order.DESC);
            assertThat(specifiers.get(1).getTarget()).isEqualTo(p.id);
        }

        @Test
        @DisplayName("applies sort: CHEAP (ASC basePrice) then ID desc tie-breaker")
        void appliesSortCheap() {
            stubSearchSelectChain(Stream.empty(), 0L);

            ProductQueryCommand.SearchProducts cmd = new ProductQueryCommand.SearchProducts(
                    null, null, ProductSearchSortType.CHEAP, 0, 5
            );

            sut.searchProducts(cmd);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<OrderSpecifier<?>> orderCaptor = ArgumentCaptor.forClass(OrderSpecifier.class);
            verify(tupleQuery).orderBy(orderCaptor.capture(), orderCaptor.capture());
            List<OrderSpecifier<?>> specifiers = orderCaptor.getAllValues();

            assertThat(specifiers.get(0).getOrder()).isEqualTo(Order.ASC);
            assertThat(specifiers.get(0).getTarget()).isEqualTo(p.basePrice);
            assertThat(specifiers.get(1).getOrder()).isEqualTo(Order.DESC);
            assertThat(specifiers.get(1).getTarget()).isEqualTo(p.id);
        }
    }

    @Nested
    @DisplayName("findDetail")
    class FindDetailTests {

        @Test
        @DisplayName("returns empty when no rows found")
        void returnsEmptyWhenNoRows() {
            when(queryFactory.select(any())).thenReturn(tupleQuery);
            when(tupleQuery.from(eq(p))).thenReturn(tupleQuery);
            when(tupleQuery.leftJoin(b)).thenReturn(tupleQuery);
            when(tupleQuery.leftJoin(po)).thenReturn(tupleQuery);
            when(tupleQuery.leftJoin(ps)).thenReturn(tupleQuery);
            when(tupleQuery.on(any())).thenReturn(tupleQuery);
            when(tupleQuery.where(eq(p.id.eq(999L)))).thenReturn(tupleQuery);
            when(tupleQuery.fetch()).thenReturn(Collections.emptyList());

            Optional<ProductQueryResult.ProductDetail> result = sut.findDetail(999L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("maps product detail without options when option id is null")
        void mapsDetailWithoutOptions() {
            Tuple row = mockTuple(Map.of(
                    p.id, 11L, p.name, "Solo Product", p.basePrice, 500, p.likeCount, 1,
                    p.brandId, 7L, b.name, "OneBrand",
                    po.id, null, ps.quantity, null, po.name, null, po.additionalPrice, null, po.productId, null
            ));
            when(queryFactory.select(any())).thenReturn(tupleQuery);
            when(tupleQuery.from(eq(p))).thenReturn(tupleQuery);
            when(tupleQuery.leftJoin(b)).thenReturn(tupleQuery);
            when(tupleQuery.leftJoin(po)).thenReturn(tupleQuery);
            when(tupleQuery.leftJoin(ps)).thenReturn(tupleQuery);
            when(tupleQuery.on(any())).thenReturn(tupleQuery);
            when(tupleQuery.where(eq(p.id.eq(11L)))).thenReturn(tupleQuery);
            when(tupleQuery.fetch()).thenReturn(List.of(row));

            Optional<ProductQueryResult.ProductDetail> result = sut.findDetail(11L);

            assertThat(result).isPresent();
            ProductQueryResult.ProductDetail d = result.get();
            assertThat(d.id()).isEqualTo(11L);
            assertThat(d.name()).isEqualTo("Solo Product");
            assertThat(d.options()).isEmpty();
        }

        @Test
        @DisplayName("aggregates multiple options for the same product")
        void aggregatesOptions() {
            Tuple r1 = mockTuple(Map.of(
                    p.id, 21L, p.name, "Multi", p.basePrice, 1000, p.likeCount, 3, p.brandId, 1L, b.name, "B",
                    po.id, 201L, po.name, "Red", po.additionalPrice, 100, po.productId, 21L, ps.quantity, 5
            ));
            Tuple r2 = mockTuple(Map.of(
                    p.id, 21L, p.name, "Multi", p.basePrice, 1000, p.likeCount, 3, p.brandId, 1L, b.name, "B",
                    po.id, 202L, po.name, "Blue", po.additionalPrice, 200, po.productId, 21L, ps.quantity, 0
            ));
            when(queryFactory.select(any())).thenReturn(tupleQuery);
            when(tupleQuery.from(eq(p))).thenReturn(tupleQuery);
            when(tupleQuery.leftJoin(b)).thenReturn(tupleQuery);
            when(tupleQuery.leftJoin(po)).thenReturn(tupleQuery);
            when(tupleQuery.leftJoin(ps)).thenReturn(tupleQuery);
            when(tupleQuery.on(any())).thenReturn(tupleQuery);
            when(tupleQuery.where(eq(p.id.eq(21L)))).thenReturn(tupleQuery);
            when(tupleQuery.fetch()).thenReturn(List.of(r1, r2));

            Optional<ProductQueryResult.ProductDetail> result = sut.findDetail(21L);

            assertThat(result).isPresent();
            ProductQueryResult.ProductDetail d = result.get();
            assertThat(d.options()).hasSize(2);
            assertThat(d.options().get(0).id()).isEqualTo(201L);
            assertThat(d.options().get(1).id()).isEqualTo(202L);
        }
    }

    @Nested
    @DisplayName("findOptions")
    class FindOptionsTests {

        @Test
        @DisplayName("returns empty when no items found")
        void returnsEmptyWhenNoItems() {
            // First select in this method returns a tupleQuery3
            when(queryFactory.select(any())).thenReturn(tupleQuery3);
            when(tupleQuery3.from(eq(po))).thenReturn(tupleQuery3);
            when(tupleQuery3.join(eq(p))).thenReturn(tupleQuery3);
            when(tupleQuery3.join(eq(ps))).thenReturn(tupleQuery3);
            when(tupleQuery3.on(any())).thenReturn(tupleQuery3);
            when(tupleQuery3.where(any())).thenReturn(tupleQuery3);
            when(tupleQuery3.stream()).thenReturn(Stream.empty());

            Optional<ProductQueryResult.ProductOptions> result = sut.findOptions(List.of(1L, 2L));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("maps items with salePrice, quantity, and productId")
        void mapsItems() {
            // Prepare a dynamic expression key to simulate 'salePrice'
            // We can't directly access the internal 'salePrice' NumberExpression, but we can
            // make the tuple respond to any NumberExpression<?> by default using doAnswer.
            Tuple t1 = mock(Tuple.class);
            Tuple t2 = mock(Tuple.class);

            // Explicit fields queried in the method
            lenient().when(t1.get(po.id)).thenReturn(301L);
            lenient().when(t1.get(ps.quantity)).thenReturn(9);
            lenient().when(t1.get(p.id)).thenReturn(41L);
            lenient().when(t2.get(po.id)).thenReturn(302L);
            lenient().when(t2.get(ps.quantity)).thenReturn(0);
            lenient().when(t2.get(p.id)).thenReturn(41L);

            // For the computed salePrice expression, respond with given ints
            // Since the exact expression instance is created in SUT, match any Expression subclass and branch on the tuple mock
            lenient().when(t1.get(any(Expression.class))).thenAnswer(inv -> {
                Expression<?> expr = inv.getArgument(0);
                if (expr == po.id || expr == ps.quantity || expr == p.id) return inv.callRealMethod();
                return 1100; // basePrice 1000 + additional 100 hypothetically
            });
            lenient().when(t2.get(any(Expression.class))).thenAnswer(inv -> {
                Expression<?> expr = inv.getArgument(0);
                if (expr == po.id || expr == ps.quantity || expr == p.id) return inv.callRealMethod();
                return 1200;
            });

            // Chain stubbing
            when(queryFactory.select(any())).thenReturn(tupleQuery3);
            when(tupleQuery3.from(eq(po))).thenReturn(tupleQuery3);
            when(tupleQuery3.join(eq(p))).thenReturn(tupleQuery3);
            when(tupleQuery3.join(eq(ps))).thenReturn(tupleQuery3);
            when(tupleQuery3.on(any())).thenReturn(tupleQuery3);
            when(tupleQuery3.where(any())).thenReturn(tupleQuery3);
            when(tupleQuery3.stream()).thenReturn(Stream.of(t1, t2));

            Optional<ProductQueryResult.ProductOptions> result = sut.findOptions(List.of(301L, 302L));

            assertThat(result).isPresent();
            ProductQueryResult.ProductOptions opts = result.get();
            assertThat(opts.items()).hasSize(2);
            assertThat(opts.items().get(0).id()).isEqualTo(301L);
            assertThat(opts.items().get(0).salePrice()).isEqualTo(1100);
            assertThat(opts.items().get(0).quantity()).isEqualTo(9);
            assertThat(opts.items().get(0).productId()).isEqualTo(41L);
            assertThat(opts.items().get(1).salePrice()).isEqualTo(1200);
        }
    }

    @Nested
    @DisplayName("Simple delegations")
    class DelegationTests {
        @Test
        @DisplayName("findProductForUpdate delegates to productRepository.findByIdForUpdate")
        void findProductForUpdateDelegates() {
            Product product = mock(Product.class);
            when(productRepository.findByIdForUpdate(77L)).thenReturn(Optional.of(product));

            Optional<Product> result = sut.findProductForUpdate(77L);

            assertThat(result).contains(product);
            verify(productRepository).findByIdForUpdate(77L);
        }

        @Test
        @DisplayName("findStocksForUpdate delegates to stockJpaRepository.findByProductOptionIdIn")
        void findStocksForUpdateDelegates() {
            ProductStock s1 = mock(ProductStock.class);
            ProductStock s2 = mock(ProductStock.class);
            List<ProductStock> stocks = List.of(s1, s2);
            when(stockJpaRepository.findByProductOptionIdIn(List.of(1L, 2L))).thenReturn(stocks);

            List<ProductStock> result = sut.findStocksForUpdate(List.of(1L, 2L));

            assertThat(result).isEqualTo(stocks);
            verify(stockJpaRepository).findByProductOptionIdIn(List.of(1L, 2L));
        }

        @Test
        @DisplayName("saveProduct delegates to productRepository.save")
        void saveProductDelegates() {
            Product input = mock(Product.class);
            Product saved = mock(Product.class);
            when(productRepository.save(input)).thenReturn(saved);

            Product result = sut.saveProduct(input);

            assertThat(result).isEqualTo(saved);
            verify(productRepository).save(input);
        }

        @Test
        @DisplayName("saveStocks delegates to stockJpaRepository.saveAll")
        void saveStocksDelegates() {
            ProductStock s1 = mock(ProductStock.class);
            List<ProductStock> input = List.of(s1);
            List<ProductStock> saved = List.of(mock(ProductStock.class));
            when(stockJpaRepository.saveAll(input)).thenReturn(saved);

            List<ProductStock> result = sut.saveStocks(input);

            assertThat(result).isEqualTo(saved);
            verify(stockJpaRepository).saveAll(input);
        }
    }
}