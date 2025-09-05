package com.loopers.application.metric;

import jakarta.annotation.Nullable;

import java.time.LocalDate;
import java.util.List;

public record MetricInput() {

    public record AggregateProduct(
            String topicName,
            List<Item> items
    ) {
        public record Item(
                String eventId,
                LocalDate date,
                Long productId,
                @Nullable Long likeCount,
                @Nullable Long saleQuantity,
                @Nullable Long viewCount
        ) {
            public static Item ofLikeCount(String eventId, LocalDate date, Long productId, Long likeCount) {
                return new Item(eventId, date, productId, likeCount, null, null);
            }

            public static Item ofSaleQuantity(String eventId, LocalDate date, Long productId, Long saleQuantity) {
                return new Item(eventId, date, productId, null, saleQuantity, null);
            }

            public static Item ofViewCount(String eventId, LocalDate date, Long productId, Long viewCount) {
                return new Item(eventId, date, productId, null, null, viewCount);
            }
        }
    }

}
