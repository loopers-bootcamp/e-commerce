package com.loopers.domain.activity;

import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

class LikedProductTest {

    @DisplayName("좋아요 한 상품을 생성할 때:")
    @Nested
    class Create {

        @DisplayName("사용자 아이디가 유효하지 않으면, BusinessException이 발생한다.")
        @NullSource
        @ParameterizedTest
        void throwException_withInvalidUserId(Long userId) {
            // given
            Long productId = 100L;

            // when & then
            assertThatException()
                    .isThrownBy(() -> LikedProduct.builder()
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
            Long userId = 1L;

            // when & then
            assertThatException()
                    .isThrownBy(() -> LikedProduct.builder()
                            .userId(userId)
                            .productId(productId)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("유효한 값이면, 좋아요 한 상품을 생성한다.")
        @CsvSource(textBlock = """
                1  | 1
                20 | 2
                3  | 300
                """, delimiter = '|')
        @ParameterizedTest
        void createNewLikedProduct_withValidValues(Long userId, Long productId) {
            // when
            LikedProduct likedProduct = LikedProduct.builder()
                    .userId(userId)
                    .productId(productId)
                    .build();

            // then
            assertThat(likedProduct).isNotNull();
            assertThat(likedProduct.getUserId()).isEqualTo(userId);
            assertThat(likedProduct.getProductId()).isEqualTo(productId);
        }

    }

}
