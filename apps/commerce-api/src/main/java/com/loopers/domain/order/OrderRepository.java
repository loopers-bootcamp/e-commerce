package com.loopers.domain.order;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Optional<Order> findOrderDetailById(UUID orderId);

    Optional<Order> findOneForUpdate(UUID orderId);

    UUID findNextOrderId();

    Order save(Order order);

}
