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
            return builder()
                    .totalPages(page.getTotalPages())
                    .totalItems(page.getTotalElements())
                    .page(page.getPageable().getPageNumber())
                    .size(page.getPageable().getPageSize())
                    .items(page.map(content -> Item.builder()
                                            .productId(content.productId())
                                            .productName(content.productName())
                                            .basePrice(content.basePrice())
                                            .likeCount(content.likeCount())
                                            .brandId(content.brandId())
                                            .brandName(content.brandName())
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

    public record GetProductDetail(
            Long productId,
            String productName,
            Integer basePrice,
            Long likeCount,
            Long brandId,
            String  brandName,
            List<Option> options
    ) {
        public static GetProductDetail from(ProductQueryResult.ProductDetail queryResult) {
            return new GetProductDetail(
                    queryResult.productId(),
                    queryResult.productName(),
                    queryResult.basePrice(),
                    queryResult.likeCount(),
                    queryResult.brandId(),
                    queryResult.brandName(),
                    queryResult.options().stream().map(Option::from).toList()
            );
        }

        public record Option(
                Long productOptionId,
                String productOptionName,
                Integer additionalPrice,
                Long productId,
                Integer stockQuantity
        ) {
            public static Option from(ProductQueryResult.ProductDetail.Option item) {
                return new Option(
                        item.productOptionId(),
                        item.productOptionName(),
                        item.additionalPrice(),
                        item.productId(),
                        item.stockQuantity()
                );
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
                    .items(queryResult.items().stream().map(Item::from).toList())
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
                        .productOptionId(item.productOptionId())
                        .salePrice(item.salePrice())
                        .stockQuantity(item.stockQuantity())
                        .productId(item.productId())
                        .build();
            }
        }
    }

}
