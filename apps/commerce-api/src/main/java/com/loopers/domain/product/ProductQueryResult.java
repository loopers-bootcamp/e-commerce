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

}
