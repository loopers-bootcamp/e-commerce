package com.loopers.domain.order;

import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

class OrderProductTest {

    @DisplayName("주문 상품을 생성할 때:")
    @Nested
    class Create {

        @DisplayName("주문 시점의 가격이 유효하지 않으면, BusinessException이 발생한다.")
        @NullSource
        @ValueSource(ints = {
                Integer.MIN_VALUE, -100, -1,
        })
        @ParameterizedTest
        void throwException_withInvalidPrice(Integer price) {
            // given
            Integer quantity = 1;
            UUID orderId = UUID.randomUUID();
            Long productOptionId = 1L;

            // when & then
            assertThatException()
                    .isThrownBy(() -> OrderProduct.builder()
                            .price(price)
                            .quantity(quantity)
                            .orderId(orderId)
                            .productOptionId(productOptionId)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("주문 수량이 유효하지 않으면, BusinessException이 발생한다.")
        @NullSource
        @ValueSource(ints = {
                Integer.MIN_VALUE, -10, -1, 0,
        })
        @ParameterizedTest
        void throwException_withInvalidQuantity(Integer quantity) {
            // given
            Integer price = 1000;
            UUID orderId = UUID.randomUUID();
            Long productOptionId = 1L;

            // when & then
            assertThatException()
                    .isThrownBy(() -> OrderProduct.builder()
                            .price(price)
                            .quantity(quantity)
                            .orderId(orderId)
                            .productOptionId(productOptionId)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("주문 아이디가 유효하지 않으면, BusinessException이 발생한다.")
        @NullSource
        @ParameterizedTest
        void throwException_withInvalidOrderId(UUID orderId) {
            // given
            Integer price = 1000;
            Integer quantity = 1;
            Long productOptionId = 1L;

            // when & then
            assertThatException()
                    .isThrownBy(() -> OrderProduct.builder()
                            .price(price)
                            .quantity(quantity)
                            .orderId(orderId)
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
            Integer price = 1000;
            Integer quantity = 1;
            UUID orderId = UUID.randomUUID();

            // when & then
            assertThatException()
                    .isThrownBy(() -> OrderProduct.builder()
                            .price(price)
                            .quantity(quantity)
                            .orderId(orderId)
                            .productOptionId(productOptionId)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("유효한 값이면, 주문 상품을 생성한다.")
        @CsvSource(textBlock = """
                1000  | 1  | 1
                25000 | 2  | 5
                500   | 10 | 10
                """, delimiter = '|')
        @ParameterizedTest
        void createNewOrderProduct_withValidValues(Integer price, Integer quantity, Long productOptionId) {
            // given
            UUID orderId = UUID.randomUUID();

            // when
            OrderProduct orderProduct = OrderProduct.builder()
                    .price(price)
                    .quantity(quantity)
                    .orderId(orderId)
                    .productOptionId(productOptionId)
                    .build();

            // then
            assertThat(orderProduct).isNotNull();
            assertThat(orderProduct.getPrice()).isEqualTo(price);
            assertThat(orderProduct.getQuantity()).isEqualTo(quantity);
            assertThat(orderProduct.getOrderId()).isEqualTo(orderId);
            assertThat(orderProduct.getProductOptionId()).isEqualTo(productOptionId);
        }

    }

}
