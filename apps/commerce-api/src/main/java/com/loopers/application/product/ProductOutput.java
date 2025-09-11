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
        @Nullable
        private final Long rank;

        public static GetProductDetail from(
                ProductResult.GetProductDetail product,
                @Nullable
                BrandResult.GetBrand brand,
                @Nullable
                Long rank
        ) {
            return GetProductDetail.builder()
                    .productId(product.productId())
                    .productName(product.productName())
                    .basePrice(product.basePrice())
                    .likeCount(product.likeCount())
                    .options(product.options().stream().map(Option::from).toList())
                    .brandId(brand == null ? null : brand.getBrandId())
                    .brandName(brand == null ? null : brand.getBrandName())
                    .brandDescription(brand == null ? null : brand.getBrandDescription())
                    .rank(rank)
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
                        .productOptionId(option.productOptionId())
                        .productOptionName(option.productOptionName())
                        .additionalPrice(option.additionalPrice())
                        .productId(option.productId())
                        .stockQuantity(option.stockQuantity())
                        .build();
            }
        }
    }

}
