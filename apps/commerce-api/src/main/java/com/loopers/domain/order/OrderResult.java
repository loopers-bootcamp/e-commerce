package com.loopers.domain.order;

import com.loopers.domain.order.attribute.OrderStatus;
import lombok.*;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderResult {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetOrderDetail {
        private final UUID orderId;
        private final Long totalPrice;
        private final Integer discountAmount;
        private final OrderStatus status;
        private final Long userId;
        private final List<Product> products;
        private final List<Long> userCouponIds;

        public static GetOrderDetail from(Order order) {
            return builder()
                    .orderId(order.getId())
                    .totalPrice(order.getTotalPrice())
                    .discountAmount(order.getDiscountAmount())
                    .status(order.getStatus())
                    .userId(order.getUserId())
                    .products(order.getProducts()
                            .stream()
                            .map(product -> Product.builder()
                                    .orderProductId(product.getId())
                                    .price(product.getPrice())
                                    .quantity(product.getQuantity())
                                    .orderId(product.getOrderId())
                                    .productOptionId(product.getProductOptionId())
                                    .build()
                            )
                            .toList()
                    )
                    .userCouponIds(order.getCoupons()
                            .stream()
                            .map(OrderCoupon::getUserCouponId)
                            .toList()
                    )
                    .build();
        }

        @Getter
        @Builder
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Product {
            private final Long orderProductId;
            private final Integer price;
            private final Integer quantity;
            private final UUID orderId;
            private final Long productOptionId;
        }
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Create {
        private final UUID orderId;
        private final Long totalPrice;
        private final OrderStatus status;
        private final Long userId;
        private final List<Product> products;

        public static Create from(Order order) {
            return builder()
                    .orderId(order.getId())
                    .totalPrice(order.getTotalPrice())
                    .status(order.getStatus())
                    .userId(order.getUserId())
                    .products(order.getProducts()
                            .stream()
                            .map(product -> Product.builder()
                                    .orderProductId(product.getId())
                                    .price(product.getPrice())
                                    .quantity(product.getQuantity())
                                    .orderId(product.getOrderId())
                                    .productOptionId(product.getProductOptionId())
                                    .build()
                            )
                            .toList()
                    )
                    .build();
        }

        @Getter
        @Builder
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Product {
            private final Long orderProductId;
            private final Integer price;
            private final Integer quantity;
            private final UUID orderId;
            private final Long productOptionId;
        }
    }

}
