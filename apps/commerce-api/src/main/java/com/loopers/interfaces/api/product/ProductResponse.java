package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductOutput;
import com.loopers.domain.product.ProductResult;
import jakarta.annotation.Nullable;
import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductResponse {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SearchProducts {
        private final Integer totalPages;
        private final Long totalItems;
        private final Integer page;
        private final Integer size;
        private final List<Item> items;

        public static SearchProducts from(ProductResult.SearchProducts result) {
            return builder()
                    .totalPages(result.getTotalPages())
                    .totalItems(result.getTotalItems())
                    .page(result.getPage())
                    .size(result.getSize())
                    .items(result.getItems()
                            .stream()
                            .map(content -> Item.builder()
                                    .productId(content.getProductId())
                                    .productName(content.getProductName())
                                    .basePrice(content.getBasePrice())
                                    .likeCount(content.getLikeCount())
                                    .brandId(content.getBrandId())
                                    .brandName(content.getBrandName())
                                    .build()
                            )
                            .toList()
                    )
                    .build();
        }

        @Getter
        @Builder
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Item {
            private final Long productId;
            private final String productName;
            private final Integer basePrice;
            private final Long likeCount;
            private final Long brandId;
            private final String brandName;
        }
    }

    // -------------------------------------------------------------------------------------------------

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

        public static GetProductDetail from(ProductOutput.GetProductDetail output) {
            List<Option> options = output.getOptions()
                    .stream()
                    .map(option -> Option.builder()
                            .productOptionId(option.getProductOptionId())
                            .productOptionName(option.getProductOptionName())
                            .additionalPrice(option.getAdditionalPrice())
                            .productId(option.getProductId())
                            .stockQuantity(option.getStockQuantity())
                            .build()
                    )
                    .toList();

            return builder()
                    .productId(output.getProductId())
                    .productName(output.getProductName())
                    .basePrice(output.getBasePrice())
                    .likeCount(output.getLikeCount())
                    .options(options)
                    .brandId(output.getBrandId())
                    .brandName(output.getBrandName())
                    .brandDescription(output.getBrandDescription())
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
        }
    }

}
