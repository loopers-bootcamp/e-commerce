package com.loopers.domain.metric;

import jakarta.annotation.Nullable;

import java.time.LocalDate;

public record MetricCommand() {

    public record AggregateProduct(
            LocalDate date,
            Long productId,
            @Nullable Long likeCount,
            @Nullable Long saleQuantity,
            @Nullable Long viewCount
    ) {
        public static AggregateProduct ofLikeCount(LocalDate date, Long productId, Long likeCount) {
            return new AggregateProduct(date, productId, likeCount, null, null);
        }

        public static AggregateProduct ofSaleQuantity(LocalDate date, Long productId, Long saleQuantity) {
            return new AggregateProduct(date, productId, null, saleQuantity, null);
        }

        public static AggregateProduct ofViewCount(LocalDate date, Long productId, Long viewCount) {
            return new AggregateProduct(date, productId, null, null, viewCount);
        }
    }

}
