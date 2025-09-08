package com.loopers.domain.product;

import java.util.List;

public record ProductCommand() {

    public record ReplaceLikeCountCaches(
            List<Item> items
    ) {
        public record Item(
                Long productId,
                Long likeCount
        ) {
        }
    }

    // -------------------------------------------------------------------------------------------------

    public record EvictProductDetailCaches(
            List<Item> items
    ) {
        public record Item(
                Long productId,
                Integer stockQuantity
        ) {
        }
    }

}
