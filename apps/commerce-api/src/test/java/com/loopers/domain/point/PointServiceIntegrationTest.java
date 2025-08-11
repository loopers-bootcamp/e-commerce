package com.loopers.domain.point;

import com.loopers.domain.point.attribute.Cause;
import com.loopers.domain.point.error.PointErrorType;
import com.loopers.domain.user.User;
import com.loopers.domain.user.attribute.Email;
import com.loopers.domain.user.attribute.Gender;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.Optional;

import static com.loopers.test.assertion.ConcurrentAssertion.assertThatConcurrence;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PointServiceIntegrationTest {

    @InjectMocks
    private final PointService sut;

    @MockitoSpyBean
    private final PointRepository pointRepository;

    private final TransactionTemplate transactionTemplate;
    private final EntityManager entityManager;
    private final DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("포인트를 조회할 때:")
    @Nested
    class GetPoint {

        @DisplayName("해당 아이디의 사용자가 없으면, Optional.empty를 반환한다.")
        @Test
        void returnEmptyOptional_whenPointDoesNotExistByUserId() {
            // given
            Long userId = 1L;

            // when
            Optional<PointResult.GetPoint> maybeResult = sut.getPoint(userId);

            // then
            assertThat(maybeResult).isEmpty();

            verify(pointRepository).findOne(userId);
        }

        @DisplayName("해당 아이디의 사용자가 있으면, 포인트를 반환한다.")
        @Test
        void returnPoint_whenPointExistsByUserId() {
            // given
            Long userId = 1L;

            Point point = Point.builder()
                    .balance(0L)
                    .userId(userId)
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(point));

            // when
            Optional<PointResult.GetPoint> maybeResult = sut.getPoint(userId);

            // then
            assertThat(maybeResult).isPresent();
            assertThat(maybeResult.get().getBalance()).isNotNull();
            assertThat(maybeResult.get().getUserId()).isEqualTo(userId);

            verify(pointRepository).findOne(userId);
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("포인트를 생성할 때:")
    @Nested
    class Create {

        @DisplayName("해당 아이디의 사용자가 있으면, BusinessException(errorType=CONFLICT)이 발생한다.")
        @Test
        void throwException_whenPointExistsByUserId() {
            // given
            Long userId = 1L;

            Point point = Point.builder()
                    .balance(0L)
                    .userId(userId)
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(point));

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

            // when
            PointResult.Create result = sut.create(userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getPointId()).isNotNull();
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

            // when & then
            assertThatException()
                    .isThrownBy(() -> sut.charge(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.NOT_FOUND);

            verify(pointRepository).findOneForUpdate(userId);
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
            Point point = Point.builder()
                    .balance(0L)
                    .userId(1L)
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(point));

            PointCommand.Charge command = PointCommand.Charge.builder()
                    .userId(point.getUserId())
                    .amount(amount)
                    .build();

            // when & then
            assertThatException()
                    .isThrownBy(() -> sut.charge(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.INVALID);

            verify(pointRepository).findOneForUpdate(point.getUserId());
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
            Point point = Point.builder()
                    .balance(balance)
                    .userId(1L)
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(point));

            PointCommand.Charge command = PointCommand.Charge.builder()
                    .userId(point.getUserId())
                    .amount(amount)
                    .build();

            // when & then
            assertThatThrownBy(() -> sut.charge(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(PointErrorType.EXCESSIVE);

            verify(pointRepository).findOneForUpdate(point.getUserId());
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
            Long userId = 1L;

            Point point = Point.builder()
                    .balance(balance)
                    .userId(userId)
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(point));

            PointCommand.Charge command = PointCommand.Charge.builder()
                    .userId(point.getUserId())
                    .amount(amount)
                    .build();

            // when
            PointResult.Charge result = sut.charge(command);

            // then
            long increasedBalance = balance + amount;
            assertThat(result).isNotNull();
            assertThat(result.getBalance()).isEqualTo(increasedBalance);

            verify(pointRepository).findOneForUpdate(userId);
            verify(pointRepository).savePoint(any(Point.class));
            verify(pointRepository).savePointHistory(any(PointHistory.class));

            PointHistory pointHistory = entityManager
                    .createQuery("SELECT ph FROM PointHistory ph WHERE ph.userId = :userId", PointHistory.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
            assertThat(pointHistory).isNotNull();
            assertThat(pointHistory.getCause()).isEqualTo(Cause.CHARGE);
            assertThat(pointHistory.getAmount()).isEqualTo(amount);
        }

        @DisplayName("사용자가 동시에 포인트를 충전하면, 잔액이 부족하지 않는 한 모든 요청을 받는다.")
        @Test
        void acceptAllRequestsAsLongAsBalanceIsSufficient_whenUserChargesPointConcurrently() {
            // given
            int threadCount = 10;

            User user = User.builder()
                    .name("gildong")
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .email(new Email("gildong.hong@example.com"))
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(user));

            Point point = Point.builder()
                    .balance(0L)
                    .userId(user.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(point));

            PointCommand.Charge command = PointCommand.Charge.builder()
                    .userId(user.getId())
                    .amount(100L)
                    .build();

            // when & then
            assertThatConcurrence()
                    .withThreadCount(threadCount)
                    .isExecutedBy(() -> sut.charge(command))
                    .isDone()
                    .hasNoError();

            verify(pointRepository, times(threadCount)).findOneForUpdate(user.getId());
            verify(pointRepository, times(threadCount)).savePoint(any(Point.class));
            verify(pointRepository, times(threadCount)).savePointHistory(any(PointHistory.class));

            Long balance = entityManager
                    .createQuery("select p.balance from Point p where p.userId = :userId", Long.class)
                    .setParameter("userId", user.getId())
                    .getSingleResult();
            assertThat(balance).isEqualTo(command.getAmount() * threadCount);

            long pointHistoryCount = entityManager
                    .createQuery("SELECT count(ph) FROM PointHistory ph WHERE ph.userId = :userId", long.class)
                    .setParameter("userId", user.getId())
                    .getSingleResult();
            assertThat(pointHistoryCount).isEqualTo(threadCount);
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

            // when & then
            assertThatException()
                    .isThrownBy(() -> sut.spend(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.NOT_FOUND);

            verify(pointRepository).findOneForUpdate(userId);
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
            Point point = Point.builder()
                    .balance(0L)
                    .userId(1L)
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(point));

            PointCommand.Spend command = PointCommand.Spend.builder()
                    .userId(point.getUserId())
                    .amount(amount)
                    .build();

            // when & then
            assertThatException()
                    .isThrownBy(() -> sut.spend(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.INVALID);

            verify(pointRepository).findOneForUpdate(point.getUserId());
            verify(pointRepository, never()).savePoint(any(Point.class));
            verify(pointRepository, never()).savePointHistory(any(PointHistory.class));
        }

        @DisplayName("""
                잔액보다 큰 금액이면,
                BusinessException(errorType=NOT_ENOUGH)이 발생한다.
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
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(point));

            PointCommand.Spend command = PointCommand.Spend.builder()
                    .userId(point.getUserId())
                    .amount(amount)
                    .build();

            // when & then
            assertThatThrownBy(() -> sut.spend(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(PointErrorType.NOT_ENOUGH);

            verify(pointRepository).findOneForUpdate(point.getUserId());
            verify(pointRepository, never()).savePoint(any(Point.class));
            verify(pointRepository, never()).savePointHistory(any(PointHistory.class));
        }

        @DisplayName("""
                유효한 금액이라면,
                감소한 잔액과 이력을 저장하고 그 잔액을 반환한다.
                """)
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
            Long userId = 1L;

            Point point = Point.builder()
                    .balance(balance)
                    .userId(userId)
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(point));

            PointCommand.Spend command = PointCommand.Spend.builder()
                    .userId(point.getUserId())
                    .amount(amount)
                    .build();

            // when
            PointResult.Spend result = sut.spend(command);

            // then
            long decreasedBalance = balance - amount;
            assertThat(result).isNotNull();
            assertThat(result.getBalance()).isEqualTo(decreasedBalance);

            verify(pointRepository).findOneForUpdate(userId);
            verify(pointRepository).savePoint(any(Point.class));
            verify(pointRepository).savePointHistory(any(PointHistory.class));

            PointHistory pointHistory = entityManager
                    .createQuery("SELECT ph FROM PointHistory ph WHERE ph.userId = :userId", PointHistory.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
            assertThat(pointHistory).isNotNull();
            assertThat(pointHistory.getCause()).isEqualTo(Cause.PURCHASE);
            assertThat(pointHistory.getAmount()).isEqualTo(amount);
        }

        @DisplayName("사용자가 동시에 포인트를 사용하면, 잔액이 부족하지 않는 한 모든 요청을 받는다.")
        @Test
        void acceptAllRequestsAsLongAsBalanceIsSufficient_whenUserSpendsPointConcurrently() {
            // given
            int threadCount = 10;

            User user = User.builder()
                    .name("gildong")
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .email(new Email("gildong.hong@example.com"))
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(user));

            Point point = Point.builder()
                    .balance(1050L)
                    .userId(user.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(point));

            PointCommand.Spend command = PointCommand.Spend.builder()
                    .userId(user.getId())
                    .amount(100L)
                    .build();

            // when & then
            assertThatConcurrence()
                    .withThreadCount(threadCount)
                    .isExecutedBy(() -> sut.spend(command))
                    .isDone()
                    .hasNoError();

            verify(pointRepository, times(threadCount)).findOneForUpdate(user.getId());
            verify(pointRepository, times(threadCount)).savePoint(any(Point.class));
            verify(pointRepository, times(threadCount)).savePointHistory(any(PointHistory.class));

            Long balance = entityManager
                    .createQuery("select p.balance from Point p where p.userId = :userId", Long.class)
                    .setParameter("userId", user.getId())
                    .getSingleResult();
            assertThat(balance).isEqualTo(50);

            long pointHistoryCount = entityManager
                    .createQuery("SELECT count(ph) FROM PointHistory ph WHERE ph.userId = :userId", long.class)
                    .setParameter("userId", user.getId())
                    .getSingleResult();
            assertThat(pointHistoryCount).isEqualTo(threadCount);
        }

    }

}
