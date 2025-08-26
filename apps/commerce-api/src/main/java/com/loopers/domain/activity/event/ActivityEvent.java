package com.loopers.domain.activity.event;

import com.loopers.domain.activity.LikedProduct;
import com.loopers.domain.saga.event.SagaEvent;

import java.util.UUID;

public record ActivityEvent() {

    public record Like(
            UUID eventKey,
            String eventName,
            Long userId,
            Long productId
    ) implements SagaEvent {
        public static Like from(LikedProduct likedProduct) {
            return new Like(
                    UUID.randomUUID(),
                    "activity.like",
                    likedProduct.getUserId(),
                    likedProduct.getProductId()
            );
        }
    }

    // -------------------------------------------------------------------------------------------------

    public record Dislike(
            UUID eventKey,
            String eventName,
            Long userId,
            Long productId
    ) implements SagaEvent {
        public static Dislike from(LikedProduct likedProduct) {
            return new Dislike(
                    UUID.randomUUID(),
                    "activity.dislike",
                    likedProduct.getUserId(),
                    likedProduct.getProductId()
            );
        }
    }

}
