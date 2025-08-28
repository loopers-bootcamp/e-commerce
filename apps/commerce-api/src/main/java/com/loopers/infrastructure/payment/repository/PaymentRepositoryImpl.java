package com.loopers.infrastructure.payment.repository;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.attempt.PaymentAttempt;
import com.loopers.domain.payment.attribute.PaymentMethod;
import com.loopers.domain.payment.attribute.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentAttemptJpaRepository paymentAttemptJpaRepository;

    @Override
    public Optional<Payment> findPayment(Long paymentId) {
        return paymentJpaRepository.findById(paymentId);
    }

    @Override
    public Optional<Payment> findPayment(UUID orderId) {
        return paymentJpaRepository.findByOrderId(orderId);
    }

    @Override
    public Optional<Payment> findPaymentForUpdate(UUID orderId) {
        return paymentJpaRepository.findByOrderIdForUpdate(orderId);
    }

    @Override
    public List<Payment> findInconclusivePayments(PaymentMethod method, List<PaymentStatus> statuses) {
        return paymentJpaRepository.findByMethodAndStatusIn(method, statuses);
    }

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public PaymentAttempt save(PaymentAttempt attempt) {
        return paymentAttemptJpaRepository.save(attempt);
    }

}
