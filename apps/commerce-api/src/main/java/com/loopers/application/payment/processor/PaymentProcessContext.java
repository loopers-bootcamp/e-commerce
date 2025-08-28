package com.loopers.application.payment.processor;

import com.loopers.domain.order.OrderResult;
import com.loopers.domain.payment.attribute.CardNumber;
import com.loopers.domain.payment.attribute.CardType;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public record PaymentProcessContext(
        Long userId,
        UUID orderId,
        @Nullable CardType cardType,
        @Nullable CardNumber cardNumber,
        List<Product> products,
        List<Long> userCouponIds,
        Long paymentAmount
) {
    public static PaymentProcessContext of(
            Long userId,
            OrderResult.GetOrderDetail order,
            CardType cardType,
            CardNumber cardNumber
    ) {
        List<Product> products = order.getProducts()
                .stream()
                .map(product -> new Product(product.getProductOptionId(), product.getQuantity()))
                .toList();

        return new PaymentProcessContext(
                userId,
                order.getOrderId(),
                cardType,
                cardNumber,
                products,
                List.copyOf(order.getUserCouponIds()),
                order.getTotalPrice() - order.getDiscountAmount()
        );
    }

    public record Product(
            Long productOptionId,
            Integer quantity
    ) {
    }
}
