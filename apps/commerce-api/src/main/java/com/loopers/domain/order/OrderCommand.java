package com.loopers.domain.order;

import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderCommand {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetOrderDetail {
        private final UUID orderId;
        @Nullable
        private final Long userId;
    }

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Create {
        private final Long userId;
        private final Long totalPrice;
        private final Integer discountAmount;
        private final List<Product> products;
        private final List<Long> userCouponIds;

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
