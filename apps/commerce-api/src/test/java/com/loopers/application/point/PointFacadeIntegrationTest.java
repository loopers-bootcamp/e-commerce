package com.loopers.application.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.PointService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.domain.user.attribute.Email;
import com.loopers.domain.user.attribute.Gender;
import com.loopers.utils.DatabaseCleanUp;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PointFacadeIntegrationTest {

    @InjectMocks
    private final PointFacade sut;

    @MockitoSpyBean
    private final PointService pointService;
    @MockitoSpyBean
    private final UserService userService;

    private final TransactionTemplate transactionTemplate;
    private final TestEntityManager testEntityManager;
    private final DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("포인트를 충전하고 조회할 때:")
    @Nested
    class ChargeAndGetPoint {

        @DisplayName("유효한 금액이라면, 증가한 잔액과 이력을 저장하고 그 잔액을 반환한다.")
        @Test
        void savePointAndHistoryAndReturnChargedPoint_whenValidAmountIsProvided() {
            // given
            User user = User.builder()
                    .name("gildong")
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .email(new Email("gildong.hong@example.com"))
                    .build();
            Long userId = transactionTemplate.execute(status -> testEntityManager.persistAndGetId(user, Long.class));

            Point point = Point.builder()
                    .balance(70_000L)
                    .userId(userId)
                    .build();
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(point));

            PointInput.Charge command = PointInput.Charge.builder()
                    .userName(user.getName())
                    .amount(30_000L)
                    .build();

            // when
            PointOutput.Charge chargeOutput = sut.charge(command);
            PointOutput.GetPoint getPointOutput = sut.getPoint(command.getUserName());

            // then
            assertThat(getPointOutput.getBalance()).isEqualTo(100_000L);
            assertThat(chargeOutput.getPointId()).isEqualTo(getPointOutput.getPointId());
            assertThat(chargeOutput.getBalance()).isEqualTo(getPointOutput.getBalance());
            assertThat(chargeOutput.getUserId()).isEqualTo(getPointOutput.getUserId());

            verify(userService, times(2)).getUser(user.getName());
            verify(pointService).getPoint(user.getId());
            verify(pointService).charge(any(PointCommand.Charge.class));
        }

    }

    @Disabled("동시성 이슈가 있다.")
    @Test
    void failOnConcurrency() {
        // given
        User user = User.builder()
                .name("gildong")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1990, 1, 1))
                .email(new Email("gildong.hong@example.com"))
                .build();
        Long userId = transactionTemplate.execute(status -> testEntityManager.persistAndGetId(user, Long.class));

        Point point = Point.builder()
                .balance(0L)
                .userId(userId)
                .build();
        transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(point));

        int threadCount = 10;
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        // when
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threadCount; i++) {
                executorService.execute(() -> {
                    try {
                        PointInput.Charge command = PointInput.Charge.builder()
                                .userName(user.getName())
                                .amount(100L)
                                .build();

                        sut.charge(command);
                    } finally {
                        countDownLatch.countDown();
                    }
                });

            }

            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // then
        verify(pointService, times(threadCount)).charge(any(PointCommand.Charge.class));

        PointOutput.GetPoint output = sut.getPoint(user.getName());
        assertThat(output.getBalance()).isEqualTo(100L * threadCount);
    }

}
