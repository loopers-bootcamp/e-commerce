package com.loopers.domain.coupon;

import com.loopers.domain.attribute.TimeRange;
import com.loopers.domain.coupon.error.CouponErrorType;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

class UserCouponTest {

    @DisplayName("사용자 쿠폰을 생성할 때:")
    @Nested
    class Create {

        @DisplayName("유효 기간이 null이면, BusinessException이 발생한다.")
        @Test
        void throwException_whenValidRangeIsNull() {
            // given
            Long userId = 1L;
            Long couponId = 1L;

            // when & then
            assertThatException()
                    .isThrownBy(() -> UserCoupon.builder()
                            .validRange(null)
                            .userId(userId)
                            .couponId(couponId)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("사용자 아이디가 null이면, BusinessException이 발생한다.")
        @Test
        void throwException_whenUserIdIsNull() {
            // given
            TimeRange validRange = TimeRange.WHENEVER;
            Long couponId = 1L;

            // when & then
            assertThatException()
                    .isThrownBy(() -> UserCoupon.builder()
                            .validRange(validRange)
                            .userId(null)
                            .couponId(couponId)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("쿠폰 아이디가 null이면, BusinessException이 발생한다.")
        @Test
        void throwException_whenCouponIdIsNull() {
            // given
            TimeRange validRange = TimeRange.WHENEVER;
            Long userId = 1L;

            // when & then
            assertThatException()
                    .isThrownBy(() -> UserCoupon.builder()
                            .validRange(validRange)
                            .userId(userId)
                            .couponId(null)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("유효한 값으로 쿠폰을 생성한다.")
        @Test
        void shouldCreateUserCoupon_withValidValues() {
            // given
            TimeRange validRange = TimeRange.WHENEVER;
            Long userId = 1L;
            Long couponId = 1L;

            // when
            UserCoupon userCoupon = UserCoupon.builder()
                    .validRange(validRange)
                    .userId(userId)
                    .couponId(couponId)
                    .build();

            // then
            assertThat(userCoupon).isNotNull();
            assertThat(userCoupon.getValidRange()).isEqualTo(validRange);
            assertThat(userCoupon.getUserId()).isEqualTo(userId);
            assertThat(userCoupon.getCouponId()).isEqualTo(couponId);
            assertThat(userCoupon.getUsed()).isFalse();
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("쿠폰 사용 가능 여부를 확인할 때:")
    @Nested
    class IsUsable {

        @DisplayName("쿠폰이 사용 가능하면, true를 반환한다.")
        @Test
        void shouldReturnTrue_whenCouponIsUsable() {
            // given
            UserCoupon userCoupon = UserCoupon.builder()
                    .validRange(TimeRange.WHENEVER)
                    .userId(1L)
                    .couponId(1L)
                    .build();

            // when
            boolean usable = userCoupon.isUsable();

            // then
            assertThat(usable).isTrue();
        }

        @DisplayName("쿠폰 유효 기간이 지났으면, false를 반환한다.")
        @Test
        void shouldReturnFalse_whenCouponIsExpired() {
            // given
            UserCoupon userCoupon = UserCoupon.builder()
                    .validRange(TimeRange.of(
                            ZonedDateTime.now().minusDays(2),
                            ZonedDateTime.now().minusDays(1)
                    ))
                    .userId(1L)
                    .couponId(1L)
                    .build();

            // when
            boolean usable = userCoupon.isUsable();

            // then
            assertThat(usable).isFalse();
        }

        @DisplayName("쿠폰이 이미 사용되었으면, false를 반환한다.")
        @Test
        void shouldReturnFalse_whenCouponIsAlreadyUsed() {
            // given
            UserCoupon userCoupon = UserCoupon.builder()
                    .validRange(TimeRange.WHENEVER)
                    .userId(1L)
                    .couponId(1L)
                    .build();
            userCoupon.use();

            // when
            boolean usable = userCoupon.isUsable();

            // then
            assertThat(usable).isFalse();
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("쿠폰을 사용할 때:")
    @Nested
    class Use {

        @DisplayName("사용 가능한 쿠폰은 상태가 변경된다.")
        @Test
        void shouldSetUsedToTrue_whenUseIsCalledOnUsableCoupon() {
            // given
            UserCoupon userCoupon = UserCoupon.builder()
                    .validRange(TimeRange.WHENEVER)
                    .userId(1L)
                    .couponId(1L)
                    .build();

            // when
            userCoupon.use();

            // then
            assertThat(userCoupon.getUsed()).isTrue();
        }

        @DisplayName("사용 불가능한 쿠폰은 BusinessException이 발생한다.")
        @Test
        void shouldThrowException_whenUseIsCalledOnUnusableCoupon() {
            // given
            UserCoupon usedCoupon = UserCoupon.builder()
                    .validRange(TimeRange.WHENEVER)
                    .userId(1L)
                    .couponId(1L)
                    .build();
            usedCoupon.use();

            UserCoupon expiredCoupon = UserCoupon.builder()
                    .validRange(TimeRange.of(
                            ZonedDateTime.now().minusDays(2),
                            ZonedDateTime.now().minusDays(1)
                    ))
                    .userId(1L)
                    .couponId(1L)
                    .build();

            // when & then
            assertThatException()
                    .isThrownBy(usedCoupon::use)
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CouponErrorType.UNAVAILABLE);
            assertThatException()
                    .isThrownBy(expiredCoupon::use)
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CouponErrorType.UNAVAILABLE);
        }

    }

}
