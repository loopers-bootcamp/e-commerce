package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderProductJpaRepository extends JpaRepository<OrderProduct, Long> {
}
