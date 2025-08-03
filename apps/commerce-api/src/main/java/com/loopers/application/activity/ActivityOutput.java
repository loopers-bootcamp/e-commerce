package com.loopers.application.activity;

import com.loopers.domain.activity.ActivityResult;
import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ActivityOutput {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetLikedProducts {
        private final List<Product> products;

        public static GetLikedProducts from(ActivityResult.GetLikedProducts result) {
            List<Product> products = result.getProducts()
                    .stream()
                    .map(product -> Product.builder()
                            .productId(product.getProductId())
                            .productName(product.getProductName())
                            .build()
                    )
                    .toList();

            return builder()
                    .products(products)
                    .build();
        }

        @Getter
        @Builder
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Product {
            private final Long productId;
            private final String productName;
        }
    }

}
