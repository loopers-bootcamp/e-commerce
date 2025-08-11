package com.loopers.domain.coupon;

import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Optional;

public interface CouponRepository {

    Optional<Coupon> findCoupon(Long couponId);

    List<Pair<Coupon, UserCoupon>> findUserCoupons(List<Long> userCouponIds);

    Optional<CouponStock> findStockForUpdate(Long couponId);

    Coupon saveCoupon(Coupon coupon);

    CouponStock saveStock(CouponStock stock);

    List<UserCoupon> saveUserCoupons(List<UserCoupon> userCoupons);

}
