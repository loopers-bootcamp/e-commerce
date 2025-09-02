package com.loopers.domain.product.event;

import com.loopers.domain.product.Product;

public interface ProductEvent {

    record LikeChanged(
            Long productId,
            Long likeCount
    ) {
        public static LikeChanged from(Product product) {
            return new LikeChanged(product.getId(), product.getLikeCount());
        }
    }

}
