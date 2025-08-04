package com.loopers.domain.activity;

import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.instancio.Select.field;

class ViewedProductTest {

    @DisplayName("조회 상품을 생성할 때:")
    @Nested
    class Create {

        @DisplayName("상품 조회 수가 유효하지 않으면, BusinessException이 발생한다.")
        @NullSource
        @ValueSource(longs = {
                Long.MIN_VALUE, -100, -1,
        })
        @ParameterizedTest
        void throwException_withInvalidViewCount(Long viewCount) {
            // given
            Long userId = 1L;
            Long productId = 100L;

            // when & then
            assertThatException()
                    .isThrownBy(() -> ViewedProduct.builder()
                            .viewCount(viewCount)
                            .userId(userId)
                            .productId(productId)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("사용자 아이디가 유효하지 않으면, BusinessException이 발생한다.")
        @NullSource
        @ParameterizedTest
        void throwException_withInvalidUserId(Long userId) {
            // given
            Long viewCount = 0L;
            Long productId = 100L;

            // when & then
            assertThatException()
                    .isThrownBy(() -> ViewedProduct.builder()
                            .viewCount(viewCount)
                            .userId(userId)
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
            Long viewCount = 0L;
            Long userId = 1L;

            // when & then
            assertThatException()
                    .isThrownBy(() -> ViewedProduct.builder()
                            .viewCount(viewCount)
                            .userId(userId)
                            .productId(productId)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("유효한 값이면, 조회 상품을 생성한다.")
        @CsvSource(textBlock = """
                0   | 1  | 1
                10  | 20 | 2
                100 | 3  | 300
                """, delimiter = '|')
        @ParameterizedTest
        void createNewViewedProduct_withValidValues(Long viewCount, Long userId, Long productId) {
            // when
            ViewedProduct viewedProduct = ViewedProduct.builder()
                    .viewCount(viewCount)
                    .userId(userId)
                    .productId(productId)
                    .build();

            // then
            assertThat(viewedProduct).isNotNull();
            assertThat(viewedProduct.getViewCount()).isEqualTo(viewCount);
            assertThat(viewedProduct.getUserId()).isEqualTo(userId);
            assertThat(viewedProduct.getProductId()).isEqualTo(productId);
        }
    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("상품 조회수를 증가시킬 때:")
    @Nested
    class View {

        @DisplayName("조회수를 1 증가시킨다.")
        @CsvSource(textBlock = """
                0    | 1
                1    | 2
                99   | 100
                9998 | 9999
                """, delimiter = '|')
        @ParameterizedTest
        void increaseViewCountByOne(long initialViewCount, long expectedViewCount) {
            // given
            ViewedProduct viewedProduct = Instancio.of(ViewedProduct.class)
                    .set(field(ViewedProduct::getViewCount), initialViewCount)
                    .create();

            // when
            viewedProduct.view();

            // then
            assertThat(viewedProduct.getViewCount()).isEqualTo(expectedViewCount);
        }

        @DisplayName("조회수가 최대값에 도달하면, 더 이상 증가하지 않는다.")
        @Test
        void doesNotIncrease_whenViewCountReachesMaxValue() {
            // given
            long maxViewCount = Long.MAX_VALUE;
            ViewedProduct viewedProduct = Instancio.of(ViewedProduct.class)
                    .set(field(ViewedProduct::getViewCount), maxViewCount)
                    .create();

            // when
            viewedProduct.view();

            // then
            assertThat(viewedProduct.getViewCount()).isEqualTo(Long.MAX_VALUE);
        }

    }

}
