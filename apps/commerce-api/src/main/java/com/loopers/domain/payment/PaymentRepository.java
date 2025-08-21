package com.loopers.domain.payment;

import com.loopers.domain.payment.attempt.PaymentAttempt;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Optional<Payment> findPayment(UUID orderId);

    Optional<Payment> findPaymentForUpdate(UUID orderId);

    Payment save(Payment payment);

    PaymentAttempt save(PaymentAttempt attempt);

}
