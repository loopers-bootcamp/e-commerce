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
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@MockitoSettings
class PointServiceTest {

    @InjectMocks
    private PointService sut;

    @Mock
    private PointRepository pointRepository;

    @DisplayName("포인트를 조회할 때:")
    @Nested
    class GetPoint {

        @DisplayName("해당 아이디의 사용자가 없으면, null을 반환한다.")
        @Test
        void returnNull_whenPointDoesNotExistByUserId() {
            // given
            Long userId = 1L;
            given(pointRepository.findPointByUserId(userId))
                    .willReturn(Optional.empty());

            // when
            Optional<PointResult.GetPoint> maybeResult = sut.getPoint(userId);

            // then
            assertThat(maybeResult).isEmpty();

            verify(pointRepository).findPointByUserId(userId);
        }

        @DisplayName("해당 아이디의 사용자가 있으면, 포인트를 반환한다.")
        @Test
        void returnPoint_whenPointExistsByUserId() {
            // given
            Long userId = 1L;
            given(pointRepository.findPointByUserId(userId))
                    .willReturn(
                            Optional.of(
                                    Point.builder()
                                            .balance(0L)
                                            .userId(userId)
                                            .build()
                            )
                    );

            // when
            Optional<PointResult.GetPoint> maybeResult = sut.getPoint(userId);

            // then
            assertThat(maybeResult).isPresent();
            assertThat(maybeResult.get().getUserId()).isEqualTo(userId);

            verify(pointRepository).findPointByUserId(userId);
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("포인트를 생성할 때:")
    @Nested
    class Create {

        @DisplayName("""
                해당 아이디의 사용자가 있으면,
                BusinessException(errorType=CONFLICT)이 발생한다.
                """)
        @Test
        void throwException_whenPointExistsByUserId() {
            // given
            Long userId = 1L;

            given(pointRepository.existsPointByUserId(userId))
                    .willReturn(true);

            // when & then
            assertThatException()
                    .isThrownBy(() -> sut.create(userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.CONFLICT);

            verify(pointRepository).existsPointByUserId(userId);
            verify(pointRepository, never()).savePoint(any(Point.class));
            verify(pointRepository, never()).savePointHistory(any(PointHistory.class));
        }


        @DisplayName("해당 아이디의 사용자가 없으면, 빈 포인트를 반환한다.")
        @Test
        void returnEmptyPoint_whenPointDoesNotExistByUserId() {
            // given
            Long userId = 1L;

            given(pointRepository.existsPointByUserId(userId))
                    .willReturn(false);

            // when
            PointResult.Create result = sut.create(userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getBalance()).isEqualTo(0L);
            assertThat(result.getUserId()).isEqualTo(userId);

            verify(pointRepository).existsPointByUserId(userId);
            verify(pointRepository).savePoint(any(Point.class));
            verify(pointRepository, never()).savePointHistory(any(PointHistory.class));
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("포인트를 충전할 때:")
    @Nested
    class Charge {

        @DisplayName("""
                해당 아이디의 사용자가 없으면,
                BusinessException(errorType=NOT_FOUND)이 발생한다.
                """)
        @Test
        void throwException_whenPointDoesNotExistByUserId() {
            // given
            Long userId = 1L;

            PointCommand.Charge command = PointCommand.Charge.builder()
                    .userId(userId)
                    .build();

            given(pointRepository.findPointByUserId(userId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatException()
                    .isThrownBy(() -> sut.charge(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.NOT_FOUND);

            verify(pointRepository).findPointByUserId(userId);
            verify(pointRepository, never()).savePoint(any(Point.class));
            verify(pointRepository, never()).savePointHistory(any(PointHistory.class));
        }

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
            PointCommand.Charge command = PointCommand.Charge.builder()
                    .amount(amount)
                    .userId(1L)
                    .build();

            given(pointRepository.findPointByUserId(any()))
                    .willReturn(
                            Optional.of(
                                    Point.builder()
                                            .balance(0L)
                                            .userId(1L)
                                            .build()
                            )
                    );

            // when & then
            assertThatException()
                    .isThrownBy(() -> sut.charge(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.INVALID);

            verify(pointRepository).findPointByUserId(any());
            verify(pointRepository, never()).savePoint(any(Point.class));
            verify(pointRepository, never()).savePointHistory(any(PointHistory.class));
        }

        @DisplayName("""
                잔액이 최대치를 초과하면,
                BusinessException(errorType=EXCESSIVE)이 발생한다.
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
            PointCommand.Charge command = PointCommand.Charge.builder()
                    .amount(amount)
                    .userId(1L)
                    .build();

            given(pointRepository.findPointByUserId(any()))
                    .willReturn(
                            Optional.of(
                                    Point.builder()
                                            .balance(balance)
                                            .userId(1L)
                                            .build()
                            )
                    );

            // when & then
            assertThatThrownBy(() -> sut.charge(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(PointErrorType.EXCESSIVE);

            verify(pointRepository).findPointByUserId(any());
            verify(pointRepository, never()).savePoint(any(Point.class));
            verify(pointRepository, never()).savePointHistory(any(PointHistory.class));
        }

        @DisplayName("유효한 금액이라면, 증가한 잔액과 이력을 저장하고 그 잔액을 반환한다.")
        @CsvSource(textBlock = """
                0           | 100_000_000
                100_000     | 500
                1000        | 1000
                500         | 100_000
                99_999_999  | 1
                """, delimiter = '|')
        @ParameterizedTest
        void savePointAndHistoryAndReturnChargedPoint_whenValidAmountIsProvided(long balance, long amount) {
            // given
            PointCommand.Charge command = PointCommand.Charge.builder()
                    .amount(amount)
                    .userId(1L)
                    .build();

            given(pointRepository.findPointByUserId(any()))
                    .willReturn(
                            Optional.of(
                                    Point.builder()
                                            .balance(balance)
                                            .userId(1L)
                                            .build()
                            )
                    );

            // when
            PointResult.Charge result = sut.charge(command);

            // then
            long increasedBalance = balance + amount;
            assertThat(result).isNotNull();
            assertThat(result.getBalance()).isEqualTo(increasedBalance);

            verify(pointRepository).findPointByUserId(any());
            verify(pointRepository).savePoint(any(Point.class));
            verify(pointRepository).savePointHistory(any(PointHistory.class));
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("포인트를 차감할 때:")
    @Nested
    class Spend {

        @DisplayName("""
                해당 아이디의 사용자가 없으면,
                BusinessException(errorType=NOT_FOUND)이 발생한다.
                """)
        @Test
        void throwException_whenPointDoesNotExistByUserId() {
            // given
            Long userId = 1L;

            PointCommand.Spend command = PointCommand.Spend.builder()
                    .userId(userId)
                    .build();

            given(pointRepository.findPointByUserId(userId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatException()
                    .isThrownBy(() -> sut.spend(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.NOT_FOUND);

            verify(pointRepository).findPointByUserId(userId);
            verify(pointRepository, never()).savePoint(any(Point.class));
            verify(pointRepository, never()).savePointHistory(any(PointHistory.class));
        }

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
            PointCommand.Spend command = PointCommand.Spend.builder()
                    .amount(amount)
                    .userId(1L)
                    .build();

            given(pointRepository.findPointByUserId(any()))
                    .willReturn(
                            Optional.of(
                                    Point.builder()
                                            .balance(0L)
                                            .userId(1L)
                                            .build()
                            )
                    );

            // when & then
            assertThatException()
                    .isThrownBy(() -> sut.spend(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.INVALID);

            verify(pointRepository).findPointByUserId(any());
            verify(pointRepository, never()).savePoint(any(Point.class));
            verify(pointRepository, never()).savePointHistory(any(PointHistory.class));
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
            PointCommand.Spend command = PointCommand.Spend.builder()
                    .amount(amount)
                    .userId(1L)
                    .build();

            given(pointRepository.findPointByUserId(any()))
                    .willReturn(
                            Optional.of(
                                    Point.builder()
                                            .balance(balance)
                                            .userId(1L)
                                            .build()
                            )
                    );

            // when & then
            assertThatThrownBy(() -> sut.spend(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(PointErrorType.NOT_ENOUGH);

            verify(pointRepository).findPointByUserId(any());
            verify(pointRepository, never()).savePoint(any(Point.class));
            verify(pointRepository, never()).savePointHistory(any(PointHistory.class));
        }

        @DisplayName("유효한 금액이라면, 감소한 잔액과 이력을 저장하고 그 잔액을 반환한다.")
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
        void savePointAndHistoryAndReturnSpentPoint_whenValidAmountIsProvided(long balance, long amount) {
            // given
            PointCommand.Spend command = PointCommand.Spend.builder()
                    .amount(amount)
                    .userId(1L)
                    .build();

            given(pointRepository.findPointByUserId(any()))
                    .willReturn(
                            Optional.of(
                                    Point.builder()
                                            .balance(balance)
                                            .userId(1L)
                                            .build()
                            )
                    );

            // when
            PointResult.Spend result = sut.spend(command);

            // then
            long decreasedBalance = balance - amount;
            assertThat(result).isNotNull();
            assertThat(result.getBalance()).isEqualTo(decreasedBalance);

            verify(pointRepository).findPointByUserId(any());
            verify(pointRepository).savePoint(any(Point.class));
            verify(pointRepository).savePointHistory(any(PointHistory.class));
        }

    }

}
