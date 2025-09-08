package com.loopers.domain.product;

import java.time.LocalDate;
import java.util.List;

public record ProductCommand() {

    public record ReplaceLikeCounts(
            List<Item> items
    ) {
        public record Item(
                Long productId,
                Long likeCount
        ) {
        }
    }

    // -------------------------------------------------------------------------------------------------

    public record RemoveDetails(
            List<Item> items
    ) {
        public record Item(
                Long productId,
                Integer stockQuantity
        ) {
        }
    }

    // -------------------------------------------------------------------------------------------------

    public record AggregateRanking(
            LocalDate date,
            Long productId,
            Long likeCount,
            Long saleQuantity,
            Long viewCount
    ) {
    }

}
