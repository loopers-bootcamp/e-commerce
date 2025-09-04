package com.loopers.interfaces.consumer.product;

public record ProductEvent() {

    record LikeChanged(
            Long productId,
            Long likeCount
    ) {
    }

    // -------------------------------------------------------------------------------------------------

    record StockChanged(
            Long productId,
            Long productOptionId,
            Integer quantity
    ) {
    }

}
