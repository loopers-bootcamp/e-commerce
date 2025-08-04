package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.point.Point;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {
}
