package com.loopers.domain.activity.event;

import com.loopers.domain.activity.LikedProduct;
import com.loopers.domain.saga.event.SagaEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public record ActivityEvent() {

    public record Like(
            String eventKey,
            String eventName,
            Long userId,
            Long productId
    ) implements SagaEvent {
        public static Like from(LikedProduct likedProduct) {
            return new Like(
                    UUID.randomUUID().toString(),
                    "activity.like",
                    likedProduct.getUserId(),
                    likedProduct.getProductId()
            );
        }
    }

    // -------------------------------------------------------------------------------------------------

    public record Dislike(
            String eventKey,
            String eventName,
            Long userId,
            Long productId
    ) implements SagaEvent {
        public static Dislike from(LikedProduct likedProduct) {
            return new Dislike(
                    UUID.randomUUID().toString(),
                    "activity.dislike",
                    likedProduct.getUserId(),
                    likedProduct.getProductId()
            );
        }
    }

    // -------------------------------------------------------------------------------------------------

    public record View(
            String eventKey,
            String eventName,
            String userName,
            Long productId
    ) implements SagaEvent {
        public static View from(String userName, Long productId) {
            // 분당 하나의 조회만 인정한다.
            long epochSecond = Instant.now().truncatedTo(ChronoUnit.MINUTES).getEpochSecond();

            return new View(
                    "user:%s|product:%d|time:%d".formatted(userName, productId, epochSecond),
                    "activity.view",
                    userName,
                    productId
            );
        }
    }

}
