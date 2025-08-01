package com.loopers.domain.order;

import java.util.UUID;

public interface OrderEventPublisher {

    void complete(UUID orderId);

}
