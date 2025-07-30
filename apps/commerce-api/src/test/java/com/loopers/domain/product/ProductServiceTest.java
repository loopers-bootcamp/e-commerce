package com.loopers.domain.product;

import com.loopers.domain.product.error.ProductErrorType;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@MockitoSettings
class ProductServiceTest {

    @InjectMocks
    private ProductService sut;

    @Mock
    private ProductRepository productRepository;

    @DisplayName("상품 상세를 조회할 때:")
    @Nested
    class GetProductDetail {

        @DisplayName("상품 아이디가 null이면, Optional.empty를 반환한다.")
        @NullSource
        @ParameterizedTest
        void returnEmptyOptional_whenProductIdIsNull(Long productId) {
            // when
            Optional<ProductResult.GetProductDetail> maybeDetail = sut.getProductDetail(productId);

            // then
            assertThat(maybeDetail).isEmpty();
            verify(productRepository, never()).findProductDetailById(productId);
        }

        @DisplayName("상품 아이디와 일치하는 상품이 없으면, Optional.empty를 반환한다.")
        @Test
        void returnEmptyOptional_whenProductDoesNotExistById() {
            // given
            Long productId = 1L;
            given(productRepository.findProductDetailById(productId))
                    .willReturn(Optional.empty());

            // when
            Optional<ProductResult.GetProductDetail> maybeDetail = sut.getProductDetail(productId);

            // then
            assertThat(maybeDetail).isEmpty();
            verify(productRepository).findProductDetailById(productId);
        }

        @DisplayName("상품 아이디와 일치하는 상품이 있으면, 상품 상세 정보를 반환한다.")
        @Test
        void returnProductDetail_whenProductExistsById() {
            // given
            Long productId = 1L;
            given(productRepository.findProductDetailById(productId))
                    .willReturn(Optional.of(
                            ProductQueryResult.ProductDetail.builder()
                                    .options(List.of())
                                    .build()
                    ));

            // when
            Optional<ProductResult.GetProductDetail> maybeDetail = sut.getProductDetail(productId);

            // then
            assertThat(maybeDetail).isPresent();
            verify(productRepository).findProductDetailById(productId);
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("입고할 때:")
    @Nested
    class AddStocks {

        @DisplayName("주어진 아이템 목록이 null이거나 비어있으면, 아무것도 하지 않는다.")
        @NullSource
        @EmptySource
        @ParameterizedTest
        void doNothing_withNullOrEmptyItems(List<ProductCommand.AddStocks.Item> items) {
            // given
            ProductCommand.AddStocks command = ProductCommand.AddStocks.builder()
                    .items(items)
                    .build();

            // when
            sut.addStocks(command);

            // then
            verify(productRepository, never()).findStocksByProductOptionIdsForUpdate(anyList());
            verify(productRepository, never()).saveStocks(anyList());
        }

        @DisplayName("상품 옵션 아이디가 중복되면, BusinessException이 발생한다.")
        @Test
        void throwException_withDuplicatedProductOptionIds() {
            // given
            Long duplicatedProductOptionId = Instancio.create(Long.class);

            List<ProductCommand.AddStocks.Item> items = List.of(
                    ProductCommand.AddStocks.Item.builder().productOptionId(duplicatedProductOptionId).amount(10).build(),
                    ProductCommand.AddStocks.Item.builder().productOptionId(Instancio.create(Long.class)).amount(5).build(),
                    ProductCommand.AddStocks.Item.builder().productOptionId(duplicatedProductOptionId).amount(20).build()
            );
            ProductCommand.AddStocks command = ProductCommand.AddStocks.builder().items(items).build();

            // when & then
            assertThatException()
                    .isThrownBy(() -> sut.addStocks(command))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);

            verify(productRepository, never()).findStocksByProductOptionIdsForUpdate(anyList());
            verify(productRepository, never()).saveStocks(anyList());
        }

        @DisplayName("일치하는 상품 재고를 찾을 수 없으면, BusinessException이 발생한다.")
        @Test
        void throwException_whenStocksAreNotFound() {
            // given
            Long savedId = 1L;
            Long nonSavedId = 2L;

            List<ProductCommand.AddStocks.Item> items = List.of(
                    ProductCommand.AddStocks.Item.builder().productOptionId(savedId).amount(10).build(),
                    ProductCommand.AddStocks.Item.builder().productOptionId(nonSavedId).amount(5).build()
            );
            ProductCommand.AddStocks command = ProductCommand.AddStocks.builder().items(items).build();

            given(productRepository.findStocksByProductOptionIdsForUpdate(anyList()))
                    .willReturn(List.of(
                            Stock.builder().productOptionId(savedId).quantity(0).build()
                    ));

            // when & then
            assertThatException()
                    .isThrownBy(() -> sut.addStocks(command))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.NOT_FOUND);

            verify(productRepository).findStocksByProductOptionIdsForUpdate(List.of(savedId, nonSavedId));
            verify(productRepository, never()).saveStocks(anyList());
        }

        @DisplayName("재고를 찾으면, 수량을 증가하고 저장한다.")
        @Test
        void increaseQuantityAsAmountAndSaveStock_whenStockIsFound() {
            // given
            Long productOptionId1 = 1L;
            Long productOptionId2 = 2L;
            int initialQuantity1 = 50;
            int initialQuantity2 = 100;
            int addAmount1 = 10;
            int addAmount2 = 20;

            Stock stock1 = Instancio.of(Stock.class)
                    .set(field(Stock::getProductOptionId), productOptionId1)
                    .set(field(Stock::getQuantity), initialQuantity1)
                    .create();
            Stock stock2 = Instancio.of(Stock.class)
                    .set(field(Stock::getProductOptionId), productOptionId2)
                    .set(field(Stock::getQuantity), initialQuantity2)
                    .create();

            List<ProductCommand.AddStocks.Item> items = List.of(
                    ProductCommand.AddStocks.Item.builder().productOptionId(productOptionId1).amount(addAmount1).build(),
                    ProductCommand.AddStocks.Item.builder().productOptionId(productOptionId2).amount(addAmount2).build()
            );
            ProductCommand.AddStocks command = ProductCommand.AddStocks.builder().items(items).build();

            // Repository Mocking
            given(productRepository.findStocksByProductOptionIdsForUpdate(List.of(productOptionId1, productOptionId2)))
                    .willReturn(List.of(stock1, stock2));

            // when
            sut.addStocks(command);

            // then
            assertThat(stock1.getQuantity()).isEqualTo(initialQuantity1 + addAmount1);
            assertThat(stock2.getQuantity()).isEqualTo(initialQuantity2 + addAmount2);

            verify(productRepository, times(1)).saveStocks(anyList());
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("출고할 때:")
    @Nested
    class DeductStocks {

        @DisplayName("주어진 아이템 목록이 null이거나 비어있으면, 아무것도 하지 않는다.")
        @NullSource
        @EmptySource
        @ParameterizedTest
        void doNothing_withNullOrEmptyItems(List<ProductCommand.DeductStocks.Item> items) {
            // given
            ProductCommand.DeductStocks command = ProductCommand.DeductStocks.builder()
                    .items(items)
                    .build();

            // when
            sut.deductStocks(command);

            // then
            verify(productRepository, never()).findStocksByProductOptionIdsForUpdate(anyList());
            verify(productRepository, never()).saveStocks(anyList());
        }

        @DisplayName("상품 옵션 아이디가 중복되면, BusinessException이 발생한다.")
        @Test
        void throwException_withDuplicatedProductOptionIds() {
            // given
            Long duplicatedProductOptionId = Instancio.create(Long.class);

            List<ProductCommand.DeductStocks.Item> items = List.of(
                    ProductCommand.DeductStocks.Item.builder().productOptionId(duplicatedProductOptionId).amount(10).build(),
                    ProductCommand.DeductStocks.Item.builder().productOptionId(Instancio.create(Long.class)).amount(5).build(),
                    ProductCommand.DeductStocks.Item.builder().productOptionId(duplicatedProductOptionId).amount(20).build()
            );
            ProductCommand.DeductStocks command = ProductCommand.DeductStocks.builder().items(items).build();

            // when & then
            assertThatException()
                    .isThrownBy(() -> sut.deductStocks(command))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);

            verify(productRepository, never()).findStocksByProductOptionIdsForUpdate(anyList());
            verify(productRepository, never()).saveStocks(anyList());
        }

        @DisplayName("일치하는 상품 재고를 찾을 수 없으면, BusinessException이 발생한다.")
        @Test
        void throwException_whenStocksAreNotFound() {
            // given
            Long savedId = 1L;
            Long nonSavedId = 2L;

            List<ProductCommand.DeductStocks.Item> items = List.of(
                    ProductCommand.DeductStocks.Item.builder().productOptionId(savedId).amount(10).build(),
                    ProductCommand.DeductStocks.Item.builder().productOptionId(nonSavedId).amount(5).build()
            );
            ProductCommand.DeductStocks command = ProductCommand.DeductStocks.builder().items(items).build();

            given(productRepository.findStocksByProductOptionIdsForUpdate(anyList()))
                    .willReturn(List.of(
                            Stock.builder().productOptionId(savedId).quantity(0).build()
                    ));

            // when & then
            assertThatException()
                    .isThrownBy(() -> sut.deductStocks(command))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.NOT_FOUND);

            verify(productRepository).findStocksByProductOptionIdsForUpdate(List.of(savedId, nonSavedId));
            verify(productRepository, never()).saveStocks(anyList());
        }

        @DisplayName("재고가 부족하면, BusinessException이 발생한다.")
        @Test
        void throwException_whenStockIsNotEnough() {
            // given
            Long productOptionId = 1L;
            int initialQuantity = 5;
            int deductAmount = initialQuantity + 5;

            Stock stock = Instancio.of(Stock.class)
                    .set(field(Stock::getQuantity), initialQuantity)
                    .set(field(Stock::getProductOptionId), productOptionId)
                    .create();

            List<ProductCommand.DeductStocks.Item> items = List.of(
                    ProductCommand.DeductStocks.Item.builder().productOptionId(productOptionId).amount(deductAmount).build()
            );
            ProductCommand.DeductStocks command = ProductCommand.DeductStocks.builder().items(items).build();

            given(productRepository.findStocksByProductOptionIdsForUpdate(List.of(productOptionId)))
                    .willReturn(List.of(stock));

            // when & then
            assertThatException()
                    .isThrownBy(() -> sut.deductStocks(command))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", ProductErrorType.NOT_ENOUGH);

            verify(productRepository).findStocksByProductOptionIdsForUpdate(List.of(productOptionId));
            verify(productRepository, never()).saveStocks(anyList());
        }

        @DisplayName("재고가 충분하면, 재고 수량을 차감하고 저장한다.")
        @Test
        void decreaseQuantityAsAmountAndSaveStock_whenStockIsEnough() {
            // given
            Long productOptionId1 = 1L;
            Long productOptionId2 = 2L;
            int initialQuantity1 = 50;
            int initialQuantity2 = 100;
            int deductAmount1 = 10;
            int deductAmount2 = 20;

            Stock stock1 = Instancio.of(Stock.class)
                    .set(field(Stock::getProductOptionId), productOptionId1)
                    .set(field(Stock::getQuantity), initialQuantity1)
                    .create();
            Stock stock2 = Instancio.of(Stock.class)
                    .set(field(Stock::getProductOptionId), productOptionId2)
                    .set(field(Stock::getQuantity), initialQuantity2)
                    .create();

            List<ProductCommand.DeductStocks.Item> items = List.of(
                    ProductCommand.DeductStocks.Item.builder().productOptionId(productOptionId1).amount(deductAmount1).build(),
                    ProductCommand.DeductStocks.Item.builder().productOptionId(productOptionId2).amount(deductAmount2).build()
            );
            ProductCommand.DeductStocks command = ProductCommand.DeductStocks.builder().items(items).build();

            // Repository Mocking
            given(productRepository.findStocksByProductOptionIdsForUpdate(List.of(productOptionId1, productOptionId2)))
                    .willReturn(List.of(stock1, stock2));

            // when
            sut.deductStocks(command);

            // then
            assertThat(stock1.getQuantity()).isEqualTo(initialQuantity1 - deductAmount1);
            assertThat(stock2.getQuantity()).isEqualTo(initialQuantity2 - deductAmount2);

            verify(productRepository, times(1)).saveStocks(anyList());
        }

    }

}
