package com.loopers.domain.coupon;

import com.loopers.domain.attribute.TimeRange;
import com.loopers.domain.coupon.attribute.DiscountPolicy;
import com.loopers.domain.coupon.attribute.DiscountRule;
import com.loopers.domain.coupon.error.CouponErrorType;
import com.loopers.support.error.BusinessException;
import com.loopers.utils.DatabaseCleanUp;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.List;

import static com.loopers.test.assertion.ConcurrentAssertion.assertThatConcurrence;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class CouponServiceIntegrationTest {

    @InjectMocks
    private final CouponService sut;

    @MockitoSpyBean
    private final CouponRepository couponRepository;

    private final TransactionTemplate transactionTemplate;
    private final EntityManager entityManager;
    private final DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("쿠폰을 사용할 때:")
    @Nested
    class Use {

        @DisplayName("사용자 쿠폰을 사용할 수 없는 상태면, BusinessException이 발생한다.")
        @Test
        void throwException_whenUserCouponIsUnavailable() {
            // given
            Coupon coupon = Coupon.builder()
                    .name("Happy Birthday!")
                    .discountPolicy(DiscountPolicy.builder()
                            .discountRule(DiscountRule.FIXED_RATE)
                            .discountValue(BigDecimal.valueOf(0.1))
                            .maxDiscountAmount(30_000)
                            .build()
                    )
                    .validityPeriod(Period.ofMonths(1))
                    .issuedRange(TimeRange.WHENEVER)
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(coupon));

            UserCoupon userCoupon = UserCoupon.builder()
                    .validRange(TimeRange.of(
                            ZonedDateTime.now().minusDays(2),
                            ZonedDateTime.now().minusDays(1)
                    ))
                    .userId(1L)
                    .couponId(coupon.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(userCoupon));

            CouponCommand.Use command = CouponCommand.Use.builder()
                    .userCouponIds(List.of(userCoupon.getId()))
                    .build();

            // when & then
            assertThatException()
                    .isThrownBy(() -> sut.use(command))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CouponErrorType.UNAVAILABLE);

            verify(couponRepository, times(1)).findUserCoupons(command.getUserCouponIds());
            verify(couponRepository, never()).saveUserCoupons(anyList());
        }

        @DisplayName("쿠폰 상태가 모두 올바르면, 사용자의 쿠폰을 사용했다고 저장한다.")
        @Test
        void saveChangedUserCouponStatus_whenAllStatusAreValid() {
            // given
            Coupon coupon = Coupon.builder()
                    .name("Happy Birthday!")
                    .discountPolicy(DiscountPolicy.builder()
                            .discountRule(DiscountRule.FIXED_RATE)
                            .discountValue(BigDecimal.valueOf(0.1))
                            .maxDiscountAmount(30_000)
                            .build()
                    )
                    .validityPeriod(Period.ofMonths(1))
                    .issuedRange(TimeRange.WHENEVER)
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(coupon));

            UserCoupon userCoupon = UserCoupon.builder()
                    .validRange(TimeRange.of(coupon.getValidityPeriod()))
                    .userId(1L)
                    .couponId(coupon.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(userCoupon));

            CouponCommand.Use command = CouponCommand.Use.builder()
                    .userCouponIds(List.of(userCoupon.getId()))
                    .build();

            // when
            sut.use(command);

            // then
            verify(couponRepository, times(1)).findUserCoupons(command.getUserCouponIds());
            verify(couponRepository, times(1)).saveUserCoupons(anyList());

            UserCoupon foundUserCoupon = entityManager.find(UserCoupon.class, userCoupon.getId());
            assertThat(foundUserCoupon).isNotNull();
            assertThat(foundUserCoupon.getUsed()).isTrue();
            assertThat(foundUserCoupon.isUsable()).isFalse();
        }

        @DisplayName("사용자가 동시에 같은 쿠폰을 사용하면, 하나의 요청만 받아들인다.")
        @Test
        void acceptOnlyOneRequest_whenUserUsesSameCouponConcurrently() {
            // given
            int threadCount = 10;

            Coupon coupon = Coupon.builder()
                    .name("Happy Birthday!")
                    .discountPolicy(DiscountPolicy.builder()
                            .discountRule(DiscountRule.FIXED_RATE)
                            .discountValue(BigDecimal.valueOf(0.1))
                            .maxDiscountAmount(30_000)
                            .build()
                    )
                    .validityPeriod(Period.ofMonths(1))
                    .issuedRange(TimeRange.WHENEVER)
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(coupon));

            UserCoupon userCoupon = UserCoupon.builder()
                    .validRange(TimeRange.of(coupon.getValidityPeriod()))
                    .userId(1L)
                    .couponId(coupon.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(userCoupon));

            CouponCommand.Use command = CouponCommand.Use.builder()
                    .userCouponIds(List.of(userCoupon.getId()))
                    .build();

            // when
            assertThatConcurrence()
                    .withThreadCount(threadCount)
                    .isExecutedBy(() -> sut.use(command))
                    .isDone()
                    .hasErrorCount(threadCount - 1)
                    .isThrownBy(OptimisticLockingFailureException.class);

            // then
            verify(couponRepository, times(threadCount)).findUserCoupons(command.getUserCouponIds());
            verify(couponRepository, atLeastOnce()).saveUserCoupons(anyList());

            UserCoupon foundUserCoupon = entityManager.find(UserCoupon.class, userCoupon.getId());
            assertThat(foundUserCoupon).isNotNull();
            assertThat(foundUserCoupon.getUsed()).isTrue();
            assertThat(foundUserCoupon.isUsable()).isFalse();
            assertThat(foundUserCoupon.getVersion()).isEqualTo(userCoupon.getVersion() + 1);
        }

    }

}
