package com.loopers.domain.payment;

public interface PaymentGateway {

    void requestTransaction(Payment payment);

}
