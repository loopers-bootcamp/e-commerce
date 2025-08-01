package com.loopers.domain.product;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductResult {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SearchProducts {
        private final Integer totalPages;
        private final Long totalItems;
        private final Integer page;
        private final Integer size;
        private final List<Item> items;

        public static SearchProducts from(Page<ProductQueryResult.Products> page) {
            return SearchProducts.builder()
                    .totalPages(page.getTotalPages())
                    .totalItems(page.getTotalElements())
                    .page(page.getPageable().getPageNumber())
                    .size(page.getPageable().getPageSize())
                    .items(page.map(content -> Item.builder()
                                            .productId(content.getProductId())
                                            .productName(content.getProductName())
                                            .basePrice(content.getBasePrice())
                                            .brandId(content.getBrandId())
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
            private final Long brandId;
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
        private final Long brandId;
        private final List<Option> options;

        public static GetProductDetail from(ProductQueryResult.ProductDetail queryResult) {
            return GetProductDetail.builder()
                    .productId(queryResult.getProductId())
                    .productName(queryResult.getProductName())
                    .basePrice(queryResult.getBasePrice())
                    .brandId(queryResult.getBrandId())
                    .options(queryResult.getOptions().stream().map(Option::from).toList())
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

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetProductOptions {
        private final List<Item> items;

        public static GetProductOptions from(ProductQueryResult.ProductOptions queryResult) {
            return GetProductOptions.builder()
                    .items(queryResult.getItems().stream().map(Item::from).toList())
                    .build();
        }

        @Getter
        @Builder
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Item {
            private final Long productOptionId;
            private final Integer salePrice;
            private final Integer stockQuantity;
            private final Long productId;

            public static Item from(ProductQueryResult.ProductOptions.Item item) {
                return Item.builder()
                        .productOptionId(item.getProductOptionId())
                        .salePrice(item.getSalePrice())
                        .stockQuantity(item.getStockQuantity())
                        .productId(item.getProductId())
                        .build();
            }
        }
    }

}
