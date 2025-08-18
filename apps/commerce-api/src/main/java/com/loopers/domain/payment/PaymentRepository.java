package com.loopers.domain.payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Optional<Payment> findPayment(UUID orderId);

    Payment save(Payment payment);

    PaymentAttempt save(PaymentAttempt attempt);

}
