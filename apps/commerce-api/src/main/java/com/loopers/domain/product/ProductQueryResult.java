package com.loopers.domain.product;

import java.util.List;

public record ProductQueryResult() {

    public record Products(
            Long productId,
            String productName,
            Integer basePrice,
            Long likeCount,
            Long brandId,
            String brandName
    ) {
    }

    // -------------------------------------------------------------------------------------------------

    public record ProductDetail(
            Long productId,
            String productName,
            Integer basePrice,
            Long likeCount,
            Long brandId,
            String brandName,
            List<Option> options
    ) {
        /**
         * For preventing cache penetration.
         */
        public static final ProductDetail EMPTY = new ProductDetail(null, null, null, null, null, null, null);

        public record Option(
                Long productOptionId,
                String productOptionName,
                Integer additionalPrice,
                Long productId,
                Integer stockQuantity
        ) {
        }
    }

    // -------------------------------------------------------------------------------------------------

    public record ProductOptions(
            List<Item> items
    ) {
        public record Item(
                Long productOptionId,
                Integer salePrice,
                Integer stockQuantity,
                Long productId
        ) {
        }
    }

}
