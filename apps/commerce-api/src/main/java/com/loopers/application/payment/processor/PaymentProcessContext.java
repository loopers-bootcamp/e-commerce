package com.loopers.application.payment.processor;

import com.loopers.domain.order.OrderResult;
import com.loopers.domain.payment.PaymentResult;
import com.loopers.domain.payment.attribute.CardNumber;
import com.loopers.domain.payment.attribute.CardType;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public record PaymentProcessContext(
        Long userId,
        Long paymentId,
        UUID orderId,
        @Nullable CardType cardType,
        @Nullable CardNumber cardNumber,
        List<Product> products,
        List<Long> userCouponIds,
        Long paymentAmount
) {
    public static PaymentProcessContext of(
            OrderResult.GetOrderDetail order,
            PaymentResult.GetPayment payment
    ) {
        List<Product> products = order.getProducts()
                .stream()
                .map(product -> new Product(product.getProductOptionId(), product.getQuantity()))
                .toList();

        return new PaymentProcessContext(
                payment.getUserId(),
                payment.getPaymentId(),
                order.getOrderId(),
                payment.getCardType(),
                payment.getCardNumber(),
                products,
                List.copyOf(order.getUserCouponIds()),
                payment.getAmount()
        );
    }

    public record Product(
            Long productOptionId,
            Integer quantity
    ) {
    }
}
