package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderCouponJpaRepository extends JpaRepository<OrderCoupon, Long> {

    List<OrderCoupon> findByOrderId(UUID orderId);

}
