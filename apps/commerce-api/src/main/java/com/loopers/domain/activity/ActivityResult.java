package com.loopers.domain.activity;

import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ActivityResult {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetLikedProducts {
        private final List<Product> products;

        public static GetLikedProducts from(List<ActivityQueryResult.GetLikedProducts> queryResults) {
            List<Product> products = queryResults.stream()
                    .map(queryResult -> Product.builder()
                            .likedProductId(queryResult.getLikedProductId())
                            .userId(queryResult.getUserId())
                            .productId(queryResult.getProductId())
                            .productName(queryResult.getProductName())
                            .build()
                    )
                    .toList();

            return GetLikedProducts.builder()
                    .products(products)
                    .build();
        }

        @Getter
        @Builder
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Product {
            private final Long likedProductId;
            private final Long userId;
            private final Long productId;
            private final String productName;
        }
    }

}
