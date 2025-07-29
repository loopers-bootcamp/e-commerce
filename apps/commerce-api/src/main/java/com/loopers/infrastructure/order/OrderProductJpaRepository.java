package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductJpaRepository extends JpaRepository<OrderProduct, Long> {
}
