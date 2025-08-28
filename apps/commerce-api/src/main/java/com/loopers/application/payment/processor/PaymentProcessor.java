package com.loopers.application.payment.processor;

import com.loopers.application.payment.PaymentOutput;
import com.loopers.domain.payment.attribute.PaymentMethod;

public interface PaymentProcessor {

    boolean supports(PaymentMethod paymentMethod);

    PaymentOutput.Pay process(PaymentProcessContext context);

}
