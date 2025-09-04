package com.loopers.domain.product.event;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductStock;

public interface ProductEvent {

    record LikeChanged(
            Long productId,
            Long likeCount
    ) {
        public static LikeChanged from(Product product) {
            return new LikeChanged(product.getId(), product.getLikeCount());
        }
    }

    // -------------------------------------------------------------------------------------------------

    record StockChanged(
            Long productId,
            Long productOptionId,
            Integer quantity
    ) {
        public static StockChanged from(ProductStock stock) {
            return new StockChanged(null, stock.getProductOptionId(), stock.getQuantity());
        }

        public StockChanged withProductId(Long productId) {
            return new StockChanged(productId, productOptionId, quantity);
        }
    }

}
