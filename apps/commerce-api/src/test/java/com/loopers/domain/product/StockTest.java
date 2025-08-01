package com.loopers.domain.product;

import com.loopers.domain.product.error.ProductErrorType;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.instancio.Select.field;

class StockTest {

    @DisplayName("재고를 생성할 때:")
    @Nested
    class Create {

        @DisplayName("재고 수량이 유효하지 않으면, BusinessException이 발생한다.")
        @NullSource
        @ValueSource(ints = {
                Integer.MIN_VALUE, -100, -1
        })
        @ParameterizedTest
        void throwException_withInvalidQuantity(Integer quantity) {
            // given
            Long productOptionId = 1L;

            // when & then
            assertThatException()
                    .isThrownBy(() -> Stock.builder()
                            .quantity(quantity)
                            .productOptionId(productOptionId)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("상품 옵션 아이디가 유효하지 않으면, BusinessException이 발생한다.")
        @NullSource
        @ParameterizedTest
        void throwException_withInvalidProductOptionId(Long productOptionId) {
            // given
            Integer quantity = 10;

            // when & then
            assertThatException()
                    .isThrownBy(() -> Stock.builder()
                            .quantity(quantity)
                            .productOptionId(productOptionId)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("유효한 값이면, 재고를 생성한다.")
        @CsvSource(textBlock = """
                0   | 1
                100 | 2
                999 | 100
                """, delimiter = '|')
        @ParameterizedTest
        void createNewStock_withValidValues(Integer quantity, Long productOptionId) {
            // when
            Stock stock = Stock.builder()
                    .quantity(quantity)
                    .productOptionId(productOptionId)
                    .build();

            // then
            assertThat(stock).isNotNull();
            assertThat(stock.getQuantity()).isEqualTo(quantity);
            assertThat(stock.getProductOptionId()).isEqualTo(productOptionId);
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("입고할 때:")
    @Nested
    class Add {

        @DisplayName("증가량이 0 이하이면, BusinessException이 발생한다.")
        @ValueSource(ints = {
                Integer.MIN_VALUE, -100, -1, 0,
        })
        @ParameterizedTest
        void throwException_whenAmountIsZeroOrNegative(int amount) {
            // given
            Stock stock = Instancio.of(Stock.class)
                    .set(field(Stock::getQuantity), 100)
                    .create();

            // when & then
            assertThatException()
                    .isThrownBy(() -> stock.add(amount))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("유효한 증가량이면, 재고 수량을 증가시킨다.")
        @CsvSource(textBlock = """
                100 | 10  | 110
                50  | 50  | 100
                0   | 1   | 1
                """, delimiter = '|')
        @ParameterizedTest
        void addQuantity_withValidAmount(int initialQuantity, int amountToAdd, int expectedQuantity) {
            // given
            Stock stock = Instancio.of(Stock.class)
                    .set(field(Stock::getQuantity), initialQuantity)
                    .create();

            // when
            stock.add(amountToAdd);

            // then
            assertThat(stock.getQuantity()).isEqualTo(expectedQuantity);
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("출고할 때:")
    @Nested
    class Deduct {

        @DisplayName("차감량이 0 이하이면, BusinessException이 발생한다.")
        @ValueSource(ints = {
                Integer.MIN_VALUE, -100, -1, 0,
        })
        @ParameterizedTest
        void throwException_whenAmountIsZeroOrNegative(int amount) {
            // given
            Stock stock = Instancio.of(Stock.class)
                    .set(field(Stock::getQuantity), 100)
                    .create();

            // when & then
            assertThatException()
                    .isThrownBy(() -> stock.deduct(amount))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("재고 수량이 부족하면, BusinessException이 발생한다.")
        @CsvSource(textBlock = """
                100 | 101
                50  | 51
                0   | 1
                """, delimiter = '|')
        @ParameterizedTest
        void throwException_whenQuantityIsNotEnough(int initialQuantity, int amountToDeduct) {
            // given
            Stock stock = Instancio.of(Stock.class)
                    .set(field(Stock::getQuantity), initialQuantity)
                    .create();

            // when & then
            assertThatException()
                    .isThrownBy(() -> stock.deduct(amountToDeduct))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", ProductErrorType.NOT_ENOUGH);
        }

        @DisplayName("유효한 차감량이고 재고가 충분하면, 재고 수량을 차감시킨다.")
        @CsvSource(textBlock = """
                100 | 10  | 90
                50  | 50  | 0
                1   | 1   | 0
                """, delimiter = '|')
        @ParameterizedTest
        void deductQuantity_withValidAmount(int initialQuantity, int amountToDeduct, int expectedQuantity) {
            // given
            Stock stock = Instancio.of(Stock.class)
                    .set(field(Stock::getQuantity), initialQuantity)
                    .create();

            // when
            stock.deduct(amountToDeduct);

            // then
            assertThat(stock.getQuantity()).isEqualTo(expectedQuantity);
        }

    }

}
