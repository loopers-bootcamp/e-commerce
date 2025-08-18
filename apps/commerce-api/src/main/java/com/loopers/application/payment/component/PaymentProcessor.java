package com.loopers.application.payment.component;

import com.loopers.domain.payment.attribute.PaymentMethod;

import java.util.UUID;

public interface PaymentProcessor {

    boolean supports(PaymentMethod paymentMethod);

    void process(Context context);

    record Context(
            UUID orderId,
            PaymentMethod paymentMethod
    ) {
    }

}
