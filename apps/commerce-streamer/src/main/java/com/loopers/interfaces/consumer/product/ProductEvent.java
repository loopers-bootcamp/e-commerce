package com.loopers.interfaces.consumer.product;

import java.util.UUID;

public record ProductEvent() {

    public record LikeChanged(
            Long productId,
            Long likeCount
    ) {
    }

    // -------------------------------------------------------------------------------------------------

    public record StockChanged(
            Long productId,
            Long productOptionId,
            Integer quantity
    ) {
    }

    // -------------------------------------------------------------------------------------------------

    public record Sale(
            UUID orderId,
            Long productId,
            Integer quantity
    ) {
    }

}
