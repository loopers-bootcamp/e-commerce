package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.CouponStock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final CouponJpaRepository couponJpaRepository;
    private final CouponStockJpaRepository couponStockJpaRepository;

    @Override
    public Optional<Coupon> findCoupon(Long couponId) {
        return couponJpaRepository.findById(couponId);
    }

    @Override
    public Optional<CouponStock> findStockForUpdate(Long couponId) {
        return couponStockJpaRepository.findByCouponId(couponId);
    }

    @Override
    public Coupon saveCoupon(Coupon coupon) {
        return couponJpaRepository.save(coupon);
    }

    @Override
    public CouponStock saveStock(CouponStock stock) {
        return couponStockJpaRepository.save(stock);
    }

}
