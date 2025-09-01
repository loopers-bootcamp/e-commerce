package com.loopers.domain.saga;

import org.jspecify.annotations.Nullable;

public record SagaCommand() {

    public record Inbound(
            String eventKey,
            String eventName,
            @Nullable Object payload
    ) {
    }

    // -------------------------------------------------------------------------------------------------

    public record Outbound(
            String eventKey,
            String eventName,
            @Nullable Object payload
    ) {
    }

}
