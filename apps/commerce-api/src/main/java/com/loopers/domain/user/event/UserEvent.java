package com.loopers.domain.user.event;

import com.loopers.domain.saga.event.SagaEvent;
import com.loopers.domain.user.User;

import java.util.UUID;

public record UserEvent() {

    public record Join(
            String eventKey,
            String eventName,
            Long userId
    ) implements SagaEvent {
        public static UserEvent.Join from(User user) {
            return new UserEvent.Join(
                    String.valueOf(user.getId().hashCode()),
                    "user.join",
                    user.getId()
            );
        }
    }

}
