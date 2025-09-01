package com.loopers.domain.order.event;

import com.loopers.domain.order.Order;
import com.loopers.domain.saga.event.SagaEvent;

import java.util.UUID;

public record OrderEvent() {

    public record Complete(
            String eventKey,
            String eventName,
            UUID orderId
    ) implements SagaEvent {
        public static Complete from(Order order) {
            return new Complete(
                    "order:%s".formatted(order.getId()),
                    "order.complete",
                    order.getId()
            );
        }
    }

}
