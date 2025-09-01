package com.loopers.domain.saga;

import org.jspecify.annotations.Nullable;

import java.util.Map;

public record SagaResult() {

    public record Inbound(
            String eventKey,
            String eventName,
            @Nullable Map<String, Object> payload,
            Boolean saved
    ) {
        public static Inbound from(Inbox inbox, boolean saved) {
            return new Inbound(
                    inbox.getEventKey(),
                    inbox.getEventName(),
                    inbox.getPayload(),
                    saved
            );
        }
    }

    // -------------------------------------------------------------------------------------------------

    public record Outbound(
            String eventKey,
            String eventName,
            @Nullable Map<String, Object> payload,
            Boolean saved
    ) {
        public static Outbound from(Outbox outbox, boolean saved) {
            return new Outbound(
                    outbox.getEventKey(),
                    outbox.getEventName(),
                    outbox.getPayload(),
                    saved
            );
        }
    }

}
