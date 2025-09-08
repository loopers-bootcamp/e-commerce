package com.loopers.domain.order.event;

import com.loopers.domain.common.DomainEvent;
import com.loopers.domain.order.Order;
import com.loopers.domain.saga.event.SagaEvent;

import java.util.UUID;

public record OrderEvent() {

    public record Complete(
            String eventKey,
            String eventName,
            UUID orderId,
            Long userId
    ) implements SagaEvent, DomainEvent {
        public static Complete from(Order order) {
            return new Complete(
                    order.getId().toString(),
                    "order.complete",
                    order.getId(),
                    order.getUserId()
            );
        }
    }

}
