package com.loopers.domain.coupon;

import com.loopers.domain.attribute.TimeRange;
import com.loopers.domain.coupon.attribute.DiscountPolicy;
import com.loopers.domain.coupon.attribute.DiscountRule;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Period;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

class CouponTest {

    @DisplayName("쿠폰을 생성할 떄:")
    @Nested
    class Create {

        @DisplayName("이름이 null이거나 비어있으면, BusinessException이 발생한다.")
        @NullSource
        @ValueSource(strings = {
                "", " "
        })
        @ParameterizedTest
        void throwException_whenNameIsInvalid(String couponName) {
            // given
            DiscountPolicy discountPolicy = DiscountPolicy.builder()
                    .discountRule(DiscountRule.FIXED_AMOUNT)
                    .discountValue(BigDecimal.valueOf(10_000))
                    .build();
            Period validityPeriod = Period.ofMonths(1);
            TimeRange issuedRange = TimeRange.WHENEVER;

            // when & then
            assertThatException()
                    .isThrownBy(() -> Coupon.builder()
                            .name(couponName)
                            .discountPolicy(discountPolicy)
                            .validityPeriod(validityPeriod)
                            .issuedRange(issuedRange)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("할인 정책이 null이면, BusinessException이 발생한다.")
        @Test
        void throwException_whenDiscountPolicyIsNull() {
            // given
            String couponName = "Test Coupon";
            Period validityPeriod = Period.ofMonths(1);
            TimeRange issuedRange = TimeRange.WHENEVER;

            // when & then
            assertThatException()
                    .isThrownBy(() -> Coupon.builder()
                            .name(couponName)
                            .discountPolicy(null)
                            .validityPeriod(validityPeriod)
                            .issuedRange(issuedRange)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("유효 기간이 올바르지 않으면, BusinessException이 발생한다.")
        @ValueSource(strings = {
                "P-1D", "P-3M", "P-50Y", "P1M-10D", "P0Y-5M-15D",
        })
        @ParameterizedTest
        void throwException_whenValidityPeriodIsInvalid(Period validityPeriod) {
            // given
            String couponName = "Test Coupon";
            DiscountPolicy discountPolicy = DiscountPolicy.builder()
                    .discountRule(DiscountRule.FIXED_AMOUNT)
                    .discountValue(BigDecimal.valueOf(10_000))
                    .build();
            TimeRange issuedRange = TimeRange.WHENEVER;

            // when & then
            assertThatException()
                    .isThrownBy(() -> Coupon.builder()
                            .name(couponName)
                            .discountPolicy(discountPolicy)
                            .validityPeriod(validityPeriod)
                            .issuedRange(issuedRange)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("발급 기간이 null이면, BusinessException이 발생한다.")
        @Test
        void throwException_whenIssuedRangeIsNull() {
            // given
            String couponName = "Test Coupon";
            DiscountPolicy discountPolicy = DiscountPolicy.builder()
                    .discountRule(DiscountRule.FIXED_AMOUNT)
                    .discountValue(BigDecimal.valueOf(10_000))
                    .build();
            Period validityPeriod = Period.ofMonths(1);

            // when & then
            assertThatException()
                    .isThrownBy(() -> Coupon.builder()
                            .name(couponName)
                            .discountPolicy(discountPolicy)
                            .validityPeriod(validityPeriod)
                            .issuedRange(null)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("유효한 값으로 쿠폰을 생성한다.")
        @Test
        void shouldCreateCoupon_withValidValues() {
            // given
            String couponName = "Test Coupon";
            DiscountPolicy discountPolicy = DiscountPolicy.builder()
                    .discountRule(DiscountRule.FIXED_AMOUNT)
                    .discountValue(BigDecimal.valueOf(10_000))
                    .build();
            Period validityPeriod = Period.ofMonths(1);
            TimeRange issuedRange = TimeRange.WHENEVER;

            // when
            Coupon coupon = Coupon.builder()
                    .name(couponName)
                    .discountPolicy(discountPolicy)
                    .validityPeriod(validityPeriod)
                    .issuedRange(issuedRange)
                    .build();

            // then
            assertThat(coupon).isNotNull();
            assertThat(coupon.getName()).isEqualTo(couponName);
            assertThat(coupon.getDiscountPolicy()).isEqualTo(discountPolicy);
            assertThat(coupon.getValidityPeriod()).isEqualTo(validityPeriod);
            assertThat(coupon.getIssuedRange()).isEqualTo(issuedRange);
            assertThat(coupon.isRevoked()).isFalse();
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("쿠폰을 폐기할 떄:")
    @Nested
    class Revoke {

        @DisplayName("폐기일시가 설정되고 폐기 여부가 true를 반환한다.")
        @Test
        void shouldSetRevokedAtAndReturnTrue() {
            // given
            String couponName = "Test Coupon";
            DiscountPolicy discountPolicy = DiscountPolicy.builder()
                    .discountRule(DiscountRule.FIXED_AMOUNT)
                    .discountValue(BigDecimal.valueOf(10_000))
                    .build();
            Period validityPeriod = Period.ofMonths(1);
            TimeRange issuedRange = TimeRange.WHENEVER;

            Coupon coupon = Coupon.builder()
                    .name(couponName)
                    .discountPolicy(discountPolicy)
                    .validityPeriod(validityPeriod)
                    .issuedRange(issuedRange)
                    .build();

            // when
            coupon.revoke();

            // then
            assertThat(coupon.getRevokedAt()).isNotNull();
            assertThat(coupon.getRevokedAt()).isBeforeOrEqualTo(ZonedDateTime.now());
            assertThat(coupon.isRevoked()).isTrue();
        }

        @DisplayName("이미 폐기된 쿠폰은 폐기일시가 변경되지 않는다. (멱등성 보장)")
        @Test
        void shouldNotChangeRevokedAt_whenCouponIsAlreadyRevoked() {
            // given
            String couponName = "Test Coupon";
            DiscountPolicy discountPolicy = DiscountPolicy.builder()
                    .discountRule(DiscountRule.FIXED_AMOUNT)
                    .discountValue(BigDecimal.valueOf(10_000))
                    .build();
            Period validityPeriod = Period.ofMonths(1);
            TimeRange issuedRange = TimeRange.WHENEVER;

            Coupon coupon = Coupon.builder()
                    .name(couponName)
                    .discountPolicy(discountPolicy)
                    .validityPeriod(validityPeriod)
                    .issuedRange(issuedRange)
                    .build();
            coupon.revoke();

            ZonedDateTime initialRevokedAt = coupon.getRevokedAt();

            // when
            coupon.revoke();

            // then
            assertThat(coupon.getRevokedAt()).isEqualTo(initialRevokedAt);
        }

    }

}
