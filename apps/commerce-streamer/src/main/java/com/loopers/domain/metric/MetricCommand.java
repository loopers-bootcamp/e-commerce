package com.loopers.domain.metric;

import java.time.LocalDate;

public record MetricCommand() {

    public record AggregateProduct(
            LocalDate date,
            Long productId,
            Long likeCount,
            Long saleQuantity,
            Long viewCount
    ) {
    }

}
