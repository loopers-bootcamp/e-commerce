package com.loopers.domain.payment;

import com.loopers.domain.payment.attempt.PaymentAttempt;
import com.loopers.domain.payment.attribute.PaymentMethod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Optional<Payment> findPayment(UUID orderId);

    Optional<Payment> findPaymentForUpdate(UUID orderId);

    List<Payment> findReadyPayments(PaymentMethod method);

    Payment save(Payment payment);

    PaymentAttempt save(PaymentAttempt attempt);

}
