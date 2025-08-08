package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface CouponStockJpaRepository extends JpaRepository<CouponStock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CouponStock> findByCouponId(Long couponId);

}
