package com.loopers.domain.point;

import com.loopers.domain.point.error.PointErrorType;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class PointTest {

    @DisplayName("포인트를 생성할 때: ")
    @Nested
    class Create {

        @DisplayName("""
                잔액이 null 또는 음수면,
                BusinessException(errorType=INVALID)이 발생한다.
                """)
        @NullSource
        @ValueSource(longs = {
                Long.MIN_VALUE, -10000, -1000, -500, -100, -1,
        })
        @ParameterizedTest
        void throwException_whenBalanceIsNullOrNegative(Long balance) {
            // when & then
            assertThatThrownBy(() ->
                    Point.builder()
                            .balance(balance)
                            .userId(1L)
                            .build())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.INVALID);
        }

        @DisplayName("""
                잔액이 최대치를 초과하면,
                BusinessException(errorType=TOO_MUCH_BALANCE)이 발생한다.
                """)
        @ValueSource(longs = {
                100_000_001, 999_999_999, Long.MAX_VALUE,
        })
        @ParameterizedTest
        void throwException_whenMaxBalanceExceeded(Long balance) {
            // when & then
            assertThatThrownBy(() ->
                    Point.builder()
                            .balance(balance)
                            .userId(1L)
                            .build())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(PointErrorType.TOO_MUCH_BALANCE);
        }

        @DisplayName("""
                사용자 아이디가 null이면,
                BusinessException(errorType=INVALID)이 발생한다.
                """)
        @Test
        void throwException_whenUserIdIsNull() {
            // when & then
            assertThatThrownBy(() ->
                    Point.builder()
                            .balance(0L)
                            .userId(null)
                            .build())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.INVALID);
        }

        @DisplayName("모든 속성이 올바르면, 신규 포인트를 반환한다.")
        @Test
        void createNewPoint_whenAllPropertiesProvidedIsValid() {
            // given
            Long balance = 0L;
            Long userId = 1L;

            // when
            Point point = Point.builder()
                    .balance(balance)
                    .userId(userId)
                    .build();

            // then
            assertThat(point.getBalance()).isEqualTo(balance);
            assertThat(point.getUserId()).isEqualTo(userId);
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("포인트를 증가할 때: ")
    @Nested
    class Increase {

        @DisplayName("""
                0 이하 금액이면,
                BusinessException(errorType=INVALID)이 발생한다.
                """)
        @ValueSource(longs = {
                Long.MIN_VALUE, -10000, -1000, -500, -100, -1, 0,
        })
        @ParameterizedTest
        void throwException_whenAmountIsZeroOrNegative(long amount) {
            // given
            Point point = Point.builder()
                    .balance(100_000L)
                    .userId(1L)
                    .build();

            // when & then
            assertThatThrownBy(() -> point.increase(amount))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.INVALID);
        }

        @DisplayName("""
                잔액이 최대치를 초과하면,
                BusinessException(errorType=TOO_MUCH_BALANCE)이 발생한다.
                """)
        @CsvSource(textBlock = """
                0           | 100_000_001
                1           | 100_000_000
                99_990_000  | 20_000
                99_999_999  | 2
                100_000_000 | 1
                """, delimiter = '|')
        @ParameterizedTest
        void throwException_whenMaxBalanceExceeded(long balance, long amount) {
            // given
            Point point = Point.builder()
                    .balance(balance)
                    .userId(1L)
                    .build();

            // when & then
            assertThatThrownBy(() -> point.increase(amount))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(PointErrorType.TOO_MUCH_BALANCE);
        }

        @DisplayName("유효한 금액이라면 잔액이 증가한다")
        @CsvSource(textBlock = """
                0           | 100_000_000
                100_000     | 500
                1000        | 1000
                500         | 100_000
                99_999_999  | 1
                """, delimiter = '|')
        @ParameterizedTest
        void increaseBalance_whenValidAmountIsProvided(long balance, long amount) {
            // given
            Point point = Point.builder()
                    .balance(balance)
                    .userId(1L)
                    .build();

            // when
            point.increase(amount);

            // then
            long increasedBalance = balance + amount;
            assertThat(point.getBalance()).isEqualTo(increasedBalance);
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("포인트를 감소할 때: ")
    @Nested
    class Decrease {

        @DisplayName("""
                0 이하 금액이면,
                BusinessException(errorType=INVALID)이 발생한다.
                """)
        @ValueSource(longs = {
                Long.MIN_VALUE, -10000, -1000, -500, -100, -1, 0,
        })
        @ParameterizedTest
        void throwException_whenAmountIsZeroOrNegative(long amount) {
            // given
            Point point = Point.builder()
                    .balance(100_000L)
                    .userId(1L)
                    .build();

            // when & then
            assertThatThrownBy(() -> point.decrease(amount))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.INVALID);
        }

        @DisplayName("""
                잔액보다 큰 금액이면,
                BusinessException(errorType=INVALID)이 발생한다.
                """)
        @CsvSource(textBlock = """
                0           | 1
                1           | 2
                500         | 1000
                10_000      | 100_000
                100_000_000 | 100_000_001
                """, delimiter = '|')
        @ParameterizedTest
        void throwException_whenBalanceIsNotEnough(long balance, long amount) {
            // given
            Point point = Point.builder()
                    .balance(balance)
                    .userId(1L)
                    .build();

            // when & then
            assertThatThrownBy(() -> point.decrease(amount))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(PointErrorType.NOT_ENOUGH_BALANCE);
        }

        @DisplayName("유효한 금액이라면, 잔액이 감소한다.")
        @CsvSource(textBlock = """
                1           | 1
                100         | 50
                500         | 400
                1000        | 999
                100_000     | 50_000
                99_999_999  | 9_999_999
                100_000_000 | 100_000_000
                """, delimiter = '|')
        @ParameterizedTest
        void decreaseBalance_whenValidAmountIsProvided(long balance, long amount) {
            // given
            Point point = Point.builder()
                    .balance(balance)
                    .userId(1L)
                    .build();

            // when
            point.decrease(amount);

            // then
            long decreasedBalance = balance - amount;
            assertThat(point.getBalance()).isEqualTo(decreasedBalance);
        }

    }

}
