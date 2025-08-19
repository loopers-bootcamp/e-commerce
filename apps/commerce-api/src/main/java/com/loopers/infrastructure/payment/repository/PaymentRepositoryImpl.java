package com.loopers.infrastructure.payment.repository;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentAttempt;
import com.loopers.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentAttemptJpaRepository paymentAttemptJpaRepository;

    @Override
    public Optional<Payment> findPayment(UUID orderId) {
        return paymentJpaRepository.findByOrderId(orderId);
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
