package com.loopers.domain.point;

import com.loopers.domain.point.attribute.Cause;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class PointHistoryTest {

    @DisplayName("포인트 이력을 생성할 때: ")
    @Nested
    class Create {

        @DisplayName("""
                원인이 null이면,
                BusinessException(errorType=INVALID)이 발생한다.
                """)
        @Test
        void throwException_whenCauseIsNull() {
            // when & then
            assertThatThrownBy(() ->
                    PointHistory.builder()
                            .cause(null)
                            .amount(100L)
                            .userId(1L)
                            .build())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.INVALID);
        }

        @DisplayName("""
                금액이 null 또는 음수면,
                BusinessException(errorType=INVALID)이 발생한다.
                """)
        @NullSource
        @ValueSource(longs = {
                Long.MIN_VALUE, -10000, -1000, -500, -100, -1,
        })
        @ParameterizedTest
        void throwException_whenAmountIsNullOrNegative(Long amount) {
            // when & then
            assertThatThrownBy(() ->
                    PointHistory.builder()
                            .cause(Cause.PURCHASE)
                            .amount(amount)
                            .userId(1L)
                            .build())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.INVALID);
        }

        @DisplayName("""
                사용자 아이디가 null이면,
                BusinessException(errorType=INVALID)이 발생한다.
                """)
        @Test
        void throwException_whenUserIdIsNull() {
            // when & then
            assertThatThrownBy(() ->
                    PointHistory.builder()
                            .cause(Cause.CHARGE)
                            .amount(100L)
                            .userId(null)
                            .build())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.INVALID);
        }

        @DisplayName("모든 속성이 올바르면, 신규 포인트 이력을 반환한다.")
        @Test
        void createNewPointHistory_whenAllPropertiesProvidedIsValid() {
            // given
            Cause cause = Cause.CHARGE;
            Long amount = 1000L;
            Long userId = 1L;

            // when
            PointHistory history = PointHistory.builder()
                    .cause(cause)
                    .amount(amount)
                    .userId(userId)
                    .build();

            // then
            assertThat(history.getCause()).isEqualTo(cause);
            assertThat(history.getAmount()).isEqualTo(amount);
            assertThat(history.getUserId()).isEqualTo(userId);
        }

    }

}
