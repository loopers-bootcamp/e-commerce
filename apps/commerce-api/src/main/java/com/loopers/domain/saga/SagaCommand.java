package com.loopers.domain.saga;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record SagaCommand() {

    public record Inbound(
            UUID eventKey,
            String eventName,
            @Nullable Object payload
    ) {
    }

    // -------------------------------------------------------------------------------------------------

    public record Outbound(
            UUID eventKey,
            String eventName,
            @Nullable Object payload
    ) {
    }

}
