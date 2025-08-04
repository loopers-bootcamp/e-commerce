package com.loopers.application.order;

import lombok.*;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderInput {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetOrderDetail {
        private final String userName;
        private final UUID orderId;
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Create {
        private final String userName;
        private final List<Product> products;

        @Getter
        @Builder
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Product {
            private final Long productOptionId;
            private final Integer quantity;
        }
    }

}
