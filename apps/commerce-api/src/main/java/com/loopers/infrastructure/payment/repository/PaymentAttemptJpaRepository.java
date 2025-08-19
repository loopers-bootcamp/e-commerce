package com.loopers.infrastructure.payment.repository;

import com.loopers.domain.payment.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAttemptJpaRepository extends JpaRepository<PaymentAttempt, Long> {
}
