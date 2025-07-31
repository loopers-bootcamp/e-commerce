package com.loopers.domain.product;

import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductQueryResult {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ProductDetail {
        private final Long productId;
        private final String productName;
        private final Integer basePrice;
        private final Long brandId;
        private final List<Option> options;

        @Getter
        @Builder
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Option {
            private final Long productOptionId;
            private final String productOptionName;
            private final Integer additionalPrice;
            private final Long productId;
            private final Integer stockQuantity;
        }

    }

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ProductOptions {
        private final List<Item> items;

        @Getter
        @Builder
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Item {
            private final Long productOptionId;
            private final Integer salePrice;
            private final Integer stockQuantity;
            private final Long productId;
        }
    }

}
