package com.loopers.domain.product;

import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import com.loopers.utils.DatabaseCleanUp;
import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ProductServiceIntegrationTest {

    private final ProductService sut;

    private final TransactionTemplate transactionTemplate;
    private final TestEntityManager testEntityManager;
    private final DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

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
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(product));

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
                testEntityManager.persist(option1);
                testEntityManager.persist(option2);
            });

            Stock stock1 = Stock.builder()
                    .quantity(100)
                    .productOptionId(option1.getId())
                    .build();
            Stock stock2 = Stock.builder()
                    .quantity(50)
                    .productOptionId(option2.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> {
                testEntityManager.persist(stock1);
                testEntityManager.persist(stock2);
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
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(product));

            ProductOption option = ProductOption.builder()
                    .name("Small")
                    .additionalPrice(0)
                    .productId(product.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(option));

            Stock stock = Stock.builder()
                    .quantity(100)
                    .productOptionId(option.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(stock));

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

            Stock foundStock = transactionTemplate.execute(status -> testEntityManager.find(Stock.class, stock.getId()));
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
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(product));

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
                testEntityManager.persist(option1);
                testEntityManager.persist(option2);
            });

            Stock stock1 = Stock.builder()
                    .quantity(100)
                    .productOptionId(option1.getId())
                    .build();
            Stock stock2 = Stock.builder()
                    .quantity(50)
                    .productOptionId(option2.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> {
                testEntityManager.persist(stock1);
                testEntityManager.persist(stock2);
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
            Stock foundStock1 = transactionTemplate.execute(status -> testEntityManager.find(Stock.class, stock1.getId()));
            Stock foundStock2 = transactionTemplate.execute(status -> testEntityManager.find(Stock.class, stock2.getId()));

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
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(product));

            ProductOption option = ProductOption.builder()
                    .name("Small")
                    .additionalPrice(0)
                    .productId(product.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(option));

            Stock stock = Stock.builder()
                    .quantity(100)
                    .productOptionId(option.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(stock));

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

            Stock foundStock = transactionTemplate.execute(status -> testEntityManager.find(Stock.class, stock.getId()));
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
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(product));

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
                testEntityManager.persist(option1);
                testEntityManager.persist(option2);
            });

            Stock stock1 = Stock.builder()
                    .quantity(100)
                    .productOptionId(option1.getId())
                    .build();
            Stock stock2 = Stock.builder()
                    .quantity(50)
                    .productOptionId(option2.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> {
                testEntityManager.persist(stock1);
                testEntityManager.persist(stock2);
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
            Stock foundStock1 = transactionTemplate.execute(status -> testEntityManager.find(Stock.class, stock1.getId()));
            Stock foundStock2 = transactionTemplate.execute(status -> testEntityManager.find(Stock.class, stock2.getId()));

            assertThat(foundStock1).isNotNull();
            assertThat(foundStock2).isNotNull();
            assertThat(foundStock1.getQuantity()).isEqualTo(100 - amountToDeduct1);
            assertThat(foundStock2.getQuantity()).isEqualTo(50 - amountToDeduct2);
        }

    }

}
