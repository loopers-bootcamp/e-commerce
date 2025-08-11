package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.*;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final CouponJpaRepository couponJpaRepository;
    private final CouponStockJpaRepository couponStockJpaRepository;
    private final UserCouponJpaRepository userCouponJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Coupon> findCoupon(Long couponId) {
        return couponJpaRepository.findById(couponId);
    }

    @Override
    public List<Pair<Coupon, UserCoupon>> findUserCoupons(List<Long> userCouponIds) {
        QCoupon c = QCoupon.coupon;
        QUserCoupon uc = QUserCoupon.userCoupon;

        List<Tuple> rows = jpaQueryFactory
                .select(c, uc)
                .from(uc)
                .join(c).on(c.id.eq(uc.couponId))
                .where(uc.id.in(userCouponIds))
                .fetch();

        List<Pair<Coupon, UserCoupon>> pairs = new ArrayList<>();
        for (Tuple row : rows) {
            Coupon coupon = row.get(c);
            UserCoupon userCoupon = row.get(uc);

            pairs.add(Pair.of(coupon, userCoupon));
        }

        return pairs;
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

    @Override
    public List<UserCoupon> saveUserCoupons(List<UserCoupon> userCoupons) {
        return userCouponJpaRepository.saveAll(userCoupons);
    }

}
