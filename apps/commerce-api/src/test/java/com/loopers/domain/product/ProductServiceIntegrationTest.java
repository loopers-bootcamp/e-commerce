package com.loopers.domain.product;

import com.loopers.domain.activity.LikedProduct;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.attribute.ProductSearchSortType;
import com.loopers.domain.product.error.ProductErrorType;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import com.loopers.utils.DatabaseCleanUp;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.loopers.test.assertion.ConcurrentAssertion.assertThatConcurrence;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ProductServiceIntegrationTest {

    @InjectMocks
    private final ProductService sut;

    @MockitoSpyBean
    private final ProductRepository productRepository;

    private final TransactionTemplate transactionTemplate;
    private final EntityManager entityManager;
    private final DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("상품 목록을 검색할 때:")
    @Nested
    class SearchProducts {

        private Brand brand;

        @BeforeEach
        void setUp() {
            brand = Brand.builder().name("Foo Company").description("Foo Company is a good company for everyone.").build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(brand));

            Product p1 = Product.builder().name("FooBarQux").basePrice(1000).brandId(brand.getId()).build();
            Product p2 = Product.builder().name("Barricade").basePrice(2000).brandId(brand.getId()).build();
            Product p3 = Product.builder().name("FC Barcelona").basePrice(3000).brandId(null).build();
            Product p4 = Product.builder().name("Iron Ballista").basePrice(4000).brandId(brand.getId() + 1).build();
            transactionTemplate.executeWithoutResult(status ->
                    Stream.of(p1, p2, p3, p4).forEach(entityManager::persist));

            List<LikedProduct> lp1 = LongStream.range(0, 10).mapToObj(i -> LikedProduct.builder().userId(i).productId(p1.getId()).build()).toList();
            List<LikedProduct> lp3 = LongStream.range(0, 30).mapToObj(i -> LikedProduct.builder().userId(i).productId(p3.getId()).build()).toList();
            List<LikedProduct> lp4 = LongStream.range(0, 40).mapToObj(i -> LikedProduct.builder().userId(i).productId(p4.getId()).build()).toList();
            transactionTemplate.executeWithoutResult(status ->
                    Stream.of(lp1, lp3, lp4).flatMap(Collection::stream).forEach(entityManager::persist));
        }

        @DisplayName("검색어를 포함하고 브랜드와 일치하는, 최신순 상품 목록을 검색한다.")
        @EnumSource(value = ProductSearchSortType.class, names = "LATEST")
        @ParameterizedTest
        void searchLatestProducts_containingKeywordAndMatchedByBrand(ProductSearchSortType sortType) {
            // given
            String keyword = "bar";
            Long brandId = brand.getId();

            ProductCommand.SearchProducts command = ProductCommand.SearchProducts.builder()
                    .keyword(keyword)
                    .brandId(brandId)
                    .sort(sortType)
                    .page(0)
                    .size(10)
                    .build();

            // when
            ProductResult.SearchProducts result = sut.searchProducts(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getTotalItems()).isEqualTo(2);
            assertThat(result.getPage()).isEqualTo(command.getPage());
            assertThat(result.getSize()).isEqualTo(command.getSize());
            assertThat(result.getItems()).hasSize(2);
            assertThat(result.getItems()).element(0)
                    .returns("Barricade", ProductResult.SearchProducts.Item::getProductName);
            assertThat(result.getItems()).element(1)
                    .returns("FooBarQux", ProductResult.SearchProducts.Item::getProductName);
        }

        @DisplayName("검색어를 포함하는, 낮은 가격순 상품 목록을 검색한다.")
        @EnumSource(value = ProductSearchSortType.class, names = "CHEAP")
        @ParameterizedTest
        void searchCheapProducts_containingKeyword(ProductSearchSortType sortType) {
            // given
            String keyword = "BAR";

            ProductCommand.SearchProducts command = ProductCommand.SearchProducts.builder()
                    .keyword(keyword)
                    .brandId(null)
                    .sort(sortType)
                    .page(0)
                    .size(10)
                    .build();

            // when
            ProductResult.SearchProducts result = sut.searchProducts(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getTotalItems()).isEqualTo(3);
            assertThat(result.getPage()).isEqualTo(command.getPage());
            assertThat(result.getSize()).isEqualTo(command.getSize());
            assertThat(result.getItems()).hasSize(3);
            assertThat(result.getItems()).element(0)
                    .returns("FooBarQux", ProductResult.SearchProducts.Item::getProductName);
            assertThat(result.getItems()).element(1)
                    .returns("Barricade", ProductResult.SearchProducts.Item::getProductName);
            assertThat(result.getItems()).element(2)
                    .returns("FC Barcelona", ProductResult.SearchProducts.Item::getProductName);
        }

        @DisplayName("인기순 상품 목록을 검색한다.")
        @EnumSource(value = ProductSearchSortType.class, names = "POPULAR")
        @ParameterizedTest
        void searchPopularProducts(ProductSearchSortType sortType) {
            // given
            ProductCommand.SearchProducts command = ProductCommand.SearchProducts.builder()
                    .keyword(null)
                    .brandId(null)
                    .sort(sortType)
                    .page(0)
                    .size(10)
                    .build();

            // when
            ProductResult.SearchProducts result = sut.searchProducts(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getTotalItems()).isEqualTo(4);
            assertThat(result.getPage()).isEqualTo(command.getPage());
            assertThat(result.getSize()).isEqualTo(command.getSize());
            assertThat(result.getItems()).hasSize(4);
            assertThat(result.getItems()).element(0)
                    .returns("Iron Ballista", ProductResult.SearchProducts.Item::getProductName);
            assertThat(result.getItems()).element(1)
                    .returns("FC Barcelona", ProductResult.SearchProducts.Item::getProductName);
            assertThat(result.getItems()).element(2)
                    .returns("FooBarQux", ProductResult.SearchProducts.Item::getProductName);
            assertThat(result.getItems()).element(3)
                    .returns("Barricade", ProductResult.SearchProducts.Item::getProductName);
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("상품 상세를 조회할 때:")
    @Nested
    class GetProductDetail {

        @DisplayName("상품 아이디와 일치하는 상품이 없으면, Optional.empty를 반환한다.")
        @Test
        void returnEmptyOptional_whenProductDoesNotExistById() {
            // given
            Long nonExistingProductId = Instancio.create(Long.class);

            // when
            Optional<ProductResult.GetProductDetail> maybeDetail = sut.getProductDetail(nonExistingProductId);

            // then
            assertThat(maybeDetail).isEmpty();
        }

        @DisplayName("상품 아이디와 일치하는 상품이 있으면, 상품 상세 정보를 반환한다.")
        @Test
        void returnProductDetail_whenProductExistsById() {
            // given
            Product product = Product.builder()
                    .name("Nike Shoes")
                    .basePrice(120_000)
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(product));

            ProductOption option1 = ProductOption.builder()
                    .name("Small")
                    .additionalPrice(0)
                    .productId(product.getId())
                    .build();
            ProductOption option2 = ProductOption.builder()
                    .name("Large")
                    .additionalPrice(1000)
                    .productId(product.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> {
                entityManager.persist(option1);
                entityManager.persist(option2);
            });

            ProductStock stock1 = ProductStock.builder()
                    .quantity(100)
                    .productOptionId(option1.getId())
                    .build();
            ProductStock stock2 = ProductStock.builder()
                    .quantity(50)
                    .productOptionId(option2.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> {
                entityManager.persist(stock1);
                entityManager.persist(stock2);
            });

            // when
            Optional<ProductResult.GetProductDetail> maybeDetail = sut.getProductDetail(product.getId());

            // then
            assertThat(maybeDetail).isPresent();
            assertThat(maybeDetail.get().getProductId()).isEqualTo(product.getId());
            assertThat(maybeDetail.get().getProductName()).isEqualTo(product.getName());
            assertThat(maybeDetail.get().getBasePrice()).isEqualTo(product.getBasePrice());
            assertThat(maybeDetail.get().getBrandId()).isEqualTo(product.getBrandId());
            assertThat(maybeDetail.get().getOptions()).hasSize(2);
            assertThat(maybeDetail.get().getOptions())
                    .extracting(ProductResult.GetProductDetail.Option::getProductOptionId)
                    .containsExactlyInAnyOrder(option1.getId(), option2.getId());
            assertThat(maybeDetail.get().getOptions())
                    .extracting(ProductResult.GetProductDetail.Option::getProductOptionName)
                    .containsExactlyInAnyOrder("Small", "Large");
            assertThat(maybeDetail.get().getOptions())
                    .extracting(ProductResult.GetProductDetail.Option::getStockQuantity)
                    .containsExactlyInAnyOrder(100, 50);
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("입고할 때:")
    @Nested
    class AddStocks {

        @DisplayName("일치하는 상품 재고를 찾을 수 없으면, BusinessException이 발생한다.")
        @Test
        void throwException_whenStocksAreNotFound() {
            // given
            Product product = Product.builder()
                    .name("Nike Shoes")
                    .basePrice(120_000)
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(product));

            ProductOption option = ProductOption.builder()
                    .name("Small")
                    .additionalPrice(0)
                    .productId(product.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(option));

            ProductStock stock = ProductStock.builder()
                    .quantity(100)
                    .productOptionId(option.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(stock));

            Long nonExistingProductOptionId = option.getId() + 1;
            List<ProductCommand.AddStocks.Item> items = List.of(
                    ProductCommand.AddStocks.Item.builder().productOptionId(option.getId()).amount(10).build(),
                    ProductCommand.AddStocks.Item.builder().productOptionId(nonExistingProductOptionId).amount(5).build()
            );
            ProductCommand.AddStocks command = ProductCommand.AddStocks.builder().items(items).build();

            // when & then
            assertThatException()
                    .isThrownBy(() -> sut.addStocks(command))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.NOT_FOUND);

            ProductStock foundStock = entityManager.find(ProductStock.class, stock.getId());
            assertThat(foundStock).isNotNull();
            assertThat(foundStock.getQuantity()).isEqualTo(stock.getQuantity());
        }

        @DisplayName("재고를 찾으면, 수량을 증가하고 저장한다.")
        @Test
        void increaseQuantityAsAmountAndSaveStock_whenStockIsFound() {
            // given
            Product product = Product.builder()
                    .name("Nike Shoes")
                    .basePrice(120_000)
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(product));

            ProductOption option1 = ProductOption.builder()
                    .name("Small")
                    .additionalPrice(0)
                    .productId(product.getId())
                    .build();
            ProductOption option2 = ProductOption.builder()
                    .name("Large")
                    .additionalPrice(1000)
                    .productId(product.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> {
                entityManager.persist(option1);
                entityManager.persist(option2);
            });

            ProductStock stock1 = ProductStock.builder()
                    .quantity(100)
                    .productOptionId(option1.getId())
                    .build();
            ProductStock stock2 = ProductStock.builder()
                    .quantity(50)
                    .productOptionId(option2.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> {
                entityManager.persist(stock1);
                entityManager.persist(stock2);
            });

            int amountToAdd1 = 10;
            int amountToAdd2 = 20;

            List<ProductCommand.AddStocks.Item> items = List.of(
                    ProductCommand.AddStocks.Item.builder().productOptionId(option1.getId()).amount(amountToAdd1).build(),
                    ProductCommand.AddStocks.Item.builder().productOptionId(option2.getId()).amount(amountToAdd2).build()
            );
            ProductCommand.AddStocks command = ProductCommand.AddStocks.builder().items(items).build();

            // when
            sut.addStocks(command);

            // then
            ProductStock foundStock1 = entityManager.find(ProductStock.class, stock1.getId());
            ProductStock foundStock2 = entityManager.find(ProductStock.class, stock2.getId());

            assertThat(foundStock1).isNotNull();
            assertThat(foundStock2).isNotNull();
            assertThat(foundStock1.getQuantity()).isEqualTo(100 + amountToAdd1);
            assertThat(foundStock2.getQuantity()).isEqualTo(50 + amountToAdd2);
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("출고할 때:")
    @Nested
    class DeductStocks {

        @DisplayName("일치하는 상품 재고를 찾을 수 없으면, BusinessException이 발생한다.")
        @Test
        void throwException_whenStocksAreNotFound() {
            // given
            Product product = Product.builder()
                    .name("Nike Shoes")
                    .basePrice(120_000)
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(product));

            ProductOption option = ProductOption.builder()
                    .name("Small")
                    .additionalPrice(0)
                    .productId(product.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(option));

            ProductStock stock = ProductStock.builder()
                    .quantity(100)
                    .productOptionId(option.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(stock));

            Long nonExistingProductOptionId = option.getId() + 1;
            List<ProductCommand.DeductStocks.Item> items = List.of(
                    ProductCommand.DeductStocks.Item.builder().productOptionId(option.getId()).amount(10).build(),
                    ProductCommand.DeductStocks.Item.builder().productOptionId(nonExistingProductOptionId).amount(5).build()
            );
            ProductCommand.DeductStocks command = ProductCommand.DeductStocks.builder().items(items).build();

            // when & then
            assertThatException()
                    .isThrownBy(() -> sut.deductStocks(command))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.NOT_FOUND);

            ProductStock foundStock = entityManager.find(ProductStock.class, stock.getId());
            assertThat(foundStock).isNotNull();
            assertThat(foundStock.getQuantity()).isEqualTo(stock.getQuantity());
        }

        @DisplayName("재고가 충분하면, 재고 수량을 차감하고 저장한다.")
        @Test
        void decreaseQuantityAsAmountAndSaveStock_whenStockIsFound() {
            // given
            Product product = Product.builder()
                    .name("Nike Shoes")
                    .basePrice(120_000)
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(product));

            ProductOption option1 = ProductOption.builder()
                    .name("Small")
                    .additionalPrice(0)
                    .productId(product.getId())
                    .build();
            ProductOption option2 = ProductOption.builder()
                    .name("Large")
                    .additionalPrice(1000)
                    .productId(product.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> {
                entityManager.persist(option1);
                entityManager.persist(option2);
            });

            ProductStock stock1 = ProductStock.builder()
                    .quantity(100)
                    .productOptionId(option1.getId())
                    .build();
            ProductStock stock2 = ProductStock.builder()
                    .quantity(50)
                    .productOptionId(option2.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> {
                entityManager.persist(stock1);
                entityManager.persist(stock2);
            });

            int amountToDeduct1 = 10;
            int amountToDeduct2 = 20;

            List<ProductCommand.DeductStocks.Item> items = List.of(
                    ProductCommand.DeductStocks.Item.builder().productOptionId(option1.getId()).amount(amountToDeduct1).build(),
                    ProductCommand.DeductStocks.Item.builder().productOptionId(option2.getId()).amount(amountToDeduct2).build()
            );
            ProductCommand.DeductStocks command = ProductCommand.DeductStocks.builder().items(items).build();

            // when
            sut.deductStocks(command);

            // then
            ProductStock foundStock1 = entityManager.find(ProductStock.class, stock1.getId());
            ProductStock foundStock2 = entityManager.find(ProductStock.class, stock2.getId());

            assertThat(foundStock1).isNotNull();
            assertThat(foundStock2).isNotNull();
            assertThat(foundStock1.getQuantity()).isEqualTo(100 - amountToDeduct1);
            assertThat(foundStock2.getQuantity()).isEqualTo(50 - amountToDeduct2);
        }

        @DisplayName("동시에 같은 상품 옵션을 출고하면, 재고가 부족해야 BusinessException이 발생한다.")
        @Test
        void throwException_withInsufficientStock_whenSameProductOptionIsShippedConcurrently() {
            // given
            int threadCount = 10;

            Product product = Product.builder()
                    .name("Nike Shoes")
                    .basePrice(120_000)
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(product));

            ProductOption option = ProductOption.builder()
                    .name("Small")
                    .additionalPrice(0)
                    .productId(product.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(option));

            ProductStock stock = ProductStock.builder()
                    .quantity(70)
                    .productOptionId(option.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(stock));

            int amountToDeduct = 10;

            List<ProductCommand.DeductStocks.Item> items = List.of(
                    ProductCommand.DeductStocks.Item.builder().productOptionId(option.getId()).amount(amountToDeduct).build()
            );
            ProductCommand.DeductStocks command = ProductCommand.DeductStocks.builder().items(items).build();

            // when & then
            assertThatConcurrence()
                    .withThreadCount(threadCount)
                    .isExecutedBy(() -> sut.deductStocks(command))
                    .isDone()
                    .hasErrorCount(3)
                    .isThrownBy(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", ProductErrorType.NOT_ENOUGH);

            List<Long> productOptionIds = items.stream().map(ProductCommand.DeductStocks.Item::getProductOptionId).toList();
            verify(productRepository, times(threadCount)).findStocksForUpdate(productOptionIds);
            verify(productRepository, times(7)).saveStocks(anyList());

            ProductStock foundStock = entityManager.find(ProductStock.class, stock.getId());
            assertThat(foundStock).isNotNull();
            assertThat(foundStock.getQuantity()).isZero();
        }

        @DisplayName("동시에 같은 상품 옵션을 출고하면, 재고가 부족하지 않는 한 모든 요청을 받는다.")
        @Test
        void acceptAllRequestsAsLongAsStockIsSufficient_whenSameProductOptionIsShippedConcurrently() {
            // given
            int threadCount = 10;

            Product product = Product.builder()
                    .name("Nike Shoes")
                    .basePrice(120_000)
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(product));

            ProductOption option = ProductOption.builder()
                    .name("Small")
                    .additionalPrice(0)
                    .productId(product.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(option));

            ProductStock stock = ProductStock.builder()
                    .quantity(120)
                    .productOptionId(option.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(stock));

            int amountToDeduct = 10;

            List<ProductCommand.DeductStocks.Item> items = List.of(
                    ProductCommand.DeductStocks.Item.builder().productOptionId(option.getId()).amount(amountToDeduct).build()
            );
            ProductCommand.DeductStocks command = ProductCommand.DeductStocks.builder().items(items).build();

            // when & then
            assertThatConcurrence()
                    .withThreadCount(threadCount)
                    .isExecutedBy(() -> sut.deductStocks(command))
                    .isDone()
                    .hasNoError();

            List<Long> productOptionIds = items.stream().map(ProductCommand.DeductStocks.Item::getProductOptionId).toList();
            verify(productRepository, times(threadCount)).findStocksForUpdate(productOptionIds);
            verify(productRepository, times(threadCount)).saveStocks(anyList());

            ProductStock foundStock = entityManager.find(ProductStock.class, stock.getId());
            assertThat(foundStock).isNotNull();
            assertThat(foundStock.getQuantity()).isEqualTo(stock.getQuantity() - (amountToDeduct * threadCount));
        }

    }

}
