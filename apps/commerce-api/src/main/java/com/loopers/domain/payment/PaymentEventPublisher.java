package com.loopers.domain.payment;

import com.loopers.domain.payment.event.PaymentEvent;

public interface PaymentEventPublisher {

    void ready(PaymentEvent.Ready event);

}
