package com.loopers.domain.metric;

import java.time.LocalDate;
import java.util.List;

public record MetricCommand() {

    public record Aggregate(
            List<Item> items
    ) {
        public record Item(
                LocalDate date,
                Long productId,
                Long likeCount,
                Long saleQuantity,
                Long viewCount
        ) {
        }
    }

}
