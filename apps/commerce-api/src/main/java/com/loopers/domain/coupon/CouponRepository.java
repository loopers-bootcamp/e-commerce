package com.loopers.domain.coupon;

import java.util.Optional;

public interface CouponRepository {

    Optional<Coupon> findCoupon(Long couponId);

    Optional<CouponStock> findStockForUpdate(Long couponId);

    Coupon saveCoupon(Coupon coupon);

    CouponStock saveStock(CouponStock stock);

}
