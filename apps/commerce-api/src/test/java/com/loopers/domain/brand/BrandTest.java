package com.loopers.domain.brand;

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

class BrandTest {

    @DisplayName("브랜드를 생성할 때:")
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
            String description = "Find your next challenge.";

            // when & then
            assertThatException()
                    .isThrownBy(() -> Brand.builder()
                            .name(name)
                            .description(description)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("설명이 유효하지 않으면, BusinessException이 발생한다.")
        @NullSource
        @ValueSource(strings = {
                "", " ",
        })
        @ParameterizedTest
        void throwException_withInvalidDescription(String description) {
            // given
            String name = "The North Face";

            // when & then
            assertThatException()
                    .isThrownBy(() -> Brand.builder()
                            .name(name)
                            .description(description)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("유효한 값이면, 브랜드를 생성한다.")
        @CsvSource(textBlock = """
                나이키   | Just Do It.
                아디다스 | Impossible Is Nothing.
                애플     | Think Different.
                """, delimiter = '|')
        @ParameterizedTest
        void createNewBrand_withValidValues(String name, String description) {
            // when
            Brand brand = Brand.builder()
                    .name(name)
                    .description(description)
                    .build();

            // then
            assertThat(brand).isNotNull();
            assertThat(brand.getName()).isEqualTo(name);
            assertThat(brand.getDescription()).isEqualTo(description);
        }

    }

}
