package com.loopers.application.order;

import com.loopers.domain.order.OrderResult;
import com.loopers.domain.order.attribute.OrderStatus;
import com.loopers.domain.payment.PaymentResult;
import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderOutput {

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

        public static GetOrderDetail from(OrderResult.GetOrderDetail order, PaymentResult.GetPayment payment) {
            return builder()
                    .orderId(order.getOrderId())
                    .totalPrice(order.getTotalPrice())
                    .status(order.getStatus())
                    .userId(order.getUserId())
                    .products(order.getProducts()
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
                    .paymentId(payment.getPaymentId())
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
        private final Integer discountAmount;
        private final OrderStatus status;

        public static Create from(OrderResult.Create result) {
            return builder()
                    .orderId(result.getOrderId())
                    .totalPrice(result.getTotalPrice())
                    .discountAmount(result.getDiscountAmount())
                    .status(result.getStatus())
                    .build();
        }
    }

}
