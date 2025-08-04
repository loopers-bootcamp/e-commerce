package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderOutput;
import com.loopers.domain.order.attribute.OrderStatus;
import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderResponse {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetOrderDetail {
        private final UUID orderId;
        private final Long totalPrice;
        private final OrderStatus status;
        private final Long userId;
        private final List<Product> products;
        @Nullable
        private final Long paymentId;

        public static GetOrderDetail from(OrderOutput.GetOrderDetail output) {
            return builder()
                    .orderId(output.getOrderId())
                    .totalPrice(output.getTotalPrice())
                    .status(output.getStatus())
                    .userId(output.getUserId())
                    .products(output.getProducts()
                            .stream()
                            .map(product -> Product.builder()
                                    .orderProductId(product.getOrderProductId())
                                    .price(product.getPrice())
                                    .quantity(product.getQuantity())
                                    .orderId(product.getOrderId())
                                    .productOptionId(product.getProductOptionId())
                                    .build()
                            )
                            .toList()
                    )
                    .paymentId(output.getPaymentId())
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

        public static Create from(OrderOutput.Create result) {
            return builder()
                    .orderId(result.getOrderId())
                    .totalPrice(result.getTotalPrice())
                    .status(result.getStatus())
                    .build();
        }
    }

}
