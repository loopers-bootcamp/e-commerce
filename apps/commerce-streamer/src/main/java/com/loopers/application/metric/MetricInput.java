package com.loopers.application.metric;

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
                Long likeCount,
                Long saleQuantity,
                Long viewCount
        ) {
            public static Item ofLikeCount(String eventId, LocalDate date, Long productId, Long likeCount) {
                return new Item(eventId, date, productId, likeCount, 0L, 0L);
            }

            public static Item ofSaleQuantity(String eventId, LocalDate date, Long productId, Long saleQuantity) {
                return new Item(eventId, date, productId, 0L, saleQuantity, 0L);
            }

            public static Item ofViewCount(String eventId, LocalDate date, Long productId, Long viewCount) {
                return new Item(eventId, date, productId, 0L, 0L, viewCount);
            }
        }
    }

}
