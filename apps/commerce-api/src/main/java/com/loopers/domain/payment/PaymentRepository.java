package com.loopers.domain.payment;

import com.loopers.domain.payment.attempt.PaymentAttempt;
import com.loopers.domain.payment.attribute.PaymentMethod;
import com.loopers.domain.payment.attribute.PaymentStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Optional<Payment> findPayment(Long paymentId);

    Optional<Payment> findPayment(UUID orderId);

    Optional<Payment> findPaymentForUpdate(Long paymentId);

    Optional<Payment> findPaymentForUpdate(UUID orderId);

    List<Payment> findInconclusivePayments(PaymentMethod method, List<PaymentStatus> statuses);

    Payment save(Payment payment);

    PaymentAttempt save(PaymentAttempt attempt);

}
