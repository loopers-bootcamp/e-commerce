package com.loopers.domain.saga;

import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public record SagaResult() {

    public record Inbound(
            UUID eventKey,
            String eventName,
            @Nullable Map<String, Object> payload
    ) {
        public static Inbound from(Inbox inbox) {
            return new Inbound(
                    inbox.getEventKey(),
                    inbox.getEventName(),
                    inbox.getPayload()
            );
        }
    }

    // -------------------------------------------------------------------------------------------------

    public record Outbound(
            UUID eventKey,
            String eventName,
            @Nullable Map<String, Object> payload
    ) {
        public static Outbound from(Outbox outbox) {
            return new Outbound(
                    outbox.getEventKey(),
                    outbox.getEventName(),
                    outbox.getPayload()
            );
        }
    }

}
