package com.loopers.domain.order;

import java.util.UUID;

public interface OrderRepository {

    UUID findNextOrderId();

    Order saveOrder(Order order);

}
