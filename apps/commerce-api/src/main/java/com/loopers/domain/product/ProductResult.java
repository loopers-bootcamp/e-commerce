package com.loopers.domain.product;

import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductResult {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetProductDetail {
        private final Long productId;
        private final String productName;
        private final Integer basePrice;
        private final Long brandId;
        private final List<Option> options;

        public static GetProductDetail from(ProductQueryResult.ProductDetail detail) {
            return GetProductDetail.builder()
                    .productId(detail.getProductId())
                    .productName(detail.getProductName())
                    .basePrice(detail.getBasePrice())
                    .brandId(detail.getBrandId())
                    .options(detail.getOptions().stream().map(Option::from).toList())
                    .build();
        }

        @Getter
        @Builder
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Option {
            private final Long productOptionId;
            private final String productOptionName;
            private final Integer additionalPrice;
            private final Long productId;
            private final Integer stockQuantity;

            public static Option from(ProductQueryResult.ProductDetail.Option item) {
                return Option.builder()
                        .productOptionId(item.getProductOptionId())
                        .productOptionName(item.getProductOptionName())
                        .additionalPrice(item.getAdditionalPrice())
                        .productId(item.getProductId())
                        .stockQuantity(item.getStockQuantity())
                        .build();
            }
        }

    }

}
