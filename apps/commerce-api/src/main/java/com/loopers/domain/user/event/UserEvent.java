package com.loopers.domain.user.event;

import com.loopers.domain.common.DomainEvent;
import com.loopers.domain.saga.event.SagaEvent;
import com.loopers.domain.user.User;

public record UserEvent() {

    public record Join(
            String eventKey,
            String eventName,
            Long userId
    ) implements SagaEvent, DomainEvent {
        public static UserEvent.Join from(User user) {
            return new UserEvent.Join(
                    "user:%d".formatted(user.getId()),
                    "user.join",
                    user.getId()
            );
        }
    }

}
