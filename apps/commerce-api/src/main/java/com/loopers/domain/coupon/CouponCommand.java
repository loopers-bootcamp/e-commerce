package com.loopers.domain.coupon;

import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CouponCommand {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetDiscountAmount {
        private final Long totalPrice;
        private final List<Long> userCouponIds;
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Use {
        private final Long userId;
        private final List<Long> userCouponIds;
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AddStocks {
        private final Long couponId;
        private final Integer amount;
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DeductStocks {
        private final Long couponId;
        private final Integer amount;
    }

}
