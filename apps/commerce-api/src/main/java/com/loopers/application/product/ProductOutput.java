package com.loopers.application.product;

import com.loopers.domain.brand.BrandResult;
import com.loopers.domain.product.ProductResult;
import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductOutput {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetProductDetail {
        private final Long productId;
        private final String productName;
        private final Integer basePrice;
        private final Long likeCount;
        private final List<Option> options;
        @Nullable
        private final Long brandId;
        @Nullable
        private final String brandName;
        @Nullable
        private final String brandDescription;

        public static GetProductDetail from(
                ProductResult.GetProductDetail product,
                long likeCount,
                @Nullable
                BrandResult.GetBrand brand
        ) {
            return GetProductDetail.builder()
                    .productId(product.getProductId())
                    .productName(product.getProductName())
                    .basePrice(product.getBasePrice())
                    .likeCount(likeCount)
                    .options(product.getOptions().stream().map(Option::from).toList())
                    .brandId(brand == null ? null : brand.getBrandId())
                    .brandName(brand == null ? null : brand.getBrandName())
                    .brandDescription(brand == null ? null : brand.getBrandDescription())
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

            public static Option from(ProductResult.GetProductDetail.Option option) {
                return Option.builder()
                        .productOptionId(option.getProductOptionId())
                        .productOptionName(option.getProductOptionName())
                        .additionalPrice(option.getAdditionalPrice())
                        .productId(option.getProductId())
                        .stockQuantity(option.getStockQuantity())
                        .build();
            }
        }
    }

}
