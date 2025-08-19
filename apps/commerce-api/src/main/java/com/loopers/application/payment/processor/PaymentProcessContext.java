package com.loopers.application.payment.processor;

import com.loopers.domain.order.OrderResult;
import com.loopers.domain.payment.attribute.PaymentMethod;

import java.util.List;
import java.util.UUID;

public record PaymentProcessContext(
        Long userId,
        UUID orderId,
        List<Product> products,
        List<Long> userCouponIds,
        Long totalPrice,
        Integer discountAmount,
        PaymentMethod paymentMethod
) {
    public static PaymentProcessContext of(
            Long userId,
            OrderResult.GetOrderDetail order,
            PaymentMethod paymentMethod
    ) {
        List<Product> products = order.getProducts()
                .stream()
                .map(product -> new Product(product.getProductOptionId(), product.getQuantity()))
                .toList();

        return new PaymentProcessContext(
                userId,
                order.getOrderId(),
                products,
                List.copyOf(order.getUserCouponIds()),
                order.getTotalPrice(),
                order.getDiscountAmount(),
                paymentMethod
        );
    }

    public record Product(
            Long productOptionId,
            Integer quantity
    ) {
    }
}
