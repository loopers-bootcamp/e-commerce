package com.loopers.domain.product;

import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

class ProductOptionTest {

    @DisplayName("상품 옵션을 생성할 때:")
    @Nested
    class Create {

        @DisplayName("이름이 유효하지 않으면, BusinessException이 발생한다.")
        @NullSource
        @ValueSource(strings = {
                "", " ",
        })
        @ParameterizedTest
        void throwException_withInvalidName(String name) {
            // given
            Integer additionalPrice = 1000;
            Long productId = 1L;

            // when & then
            assertThatException()
                    .isThrownBy(() -> ProductOption.builder()
                            .name(name)
                            .additionalPrice(additionalPrice)
                            .productId(productId)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("추가 가격이 유효하지 않으면, BusinessException이 발생한다.")
        @NullSource
        @ValueSource(ints = {
                Integer.MIN_VALUE, -1000, -1,
        })
        @ParameterizedTest
        void throwException_withInvalidAdditionalPrice(Integer additionalPrice) {
            // given
            String name = "(Color: Red)";
            Long productId = 1L;

            // when & then
            assertThatException()
                    .isThrownBy(() -> ProductOption.builder()
                            .name(name)
                            .additionalPrice(additionalPrice)
                            .productId(productId)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("상품 아이디가 유효하지 않으면, BusinessException이 발생한다.")
        @NullSource
        @ParameterizedTest
        void throwException_withInvalidProductId(Long productId) {
            // given
            String name = "(Size: 105)";
            Integer additionalPrice = 1000;

            // when & then
            assertThatException()
                    .isThrownBy(() -> ProductOption.builder()
                            .name(name)
                            .additionalPrice(additionalPrice)
                            .productId(productId)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("유효한 값이면, 상품 옵션을 생성한다.")
        @CsvSource(textBlock = """
                블랙   | 0      | 1
                화이트 | 500    | 2
                S      | 10_000 | 10
                L      | 10_000 | 10
                XL     | 10_000 | 10
                90     | 1000   | 100
                100    | 5000   | 100
                """, delimiter = '|')
        @ParameterizedTest
        void createNewProductOption_withValidValues(String name, Integer additionalPrice, Long productId) {
            // when
            ProductOption option = ProductOption.builder()
                    .name(name)
                    .additionalPrice(additionalPrice)
                    .productId(productId)
                    .build();

            // then
            assertThat(option).isNotNull();
            assertThat(option.getName()).isEqualTo(name);
            assertThat(option.getAdditionalPrice()).isEqualTo(additionalPrice);
            assertThat(option.getProductId()).isEqualTo(productId);
        }
    }

}
