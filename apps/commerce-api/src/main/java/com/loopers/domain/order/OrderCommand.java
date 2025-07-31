package com.loopers.domain.order;

import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderCommand {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Create {
        private final Long userId;
        private final Long totalPrice;
        private final List<Product> products;

        @Getter
        @Builder
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Product {
            private final Long productOptionId;
            private final Integer quantity;
            private final Integer price;
        }
    }

}
