package com.loopers.interfaces.consumer.metric;

public record ActivityEvent() {

    public record Like(
            String eventKey,
            String eventName,
            Long userId,
            Long productId
    ) {
    }

    // -------------------------------------------------------------------------------------------------

    public record Dislike(
            String eventKey,
            String eventName,
            Long userId,
            Long productId
    ) {
    }

    // -------------------------------------------------------------------------------------------------

    public record View(
            String eventKey,
            String eventName,
            Long userId,
            Long productId
    ) {
    }

}
