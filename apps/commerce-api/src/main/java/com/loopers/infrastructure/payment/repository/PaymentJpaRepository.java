package com.loopers.infrastructure.payment.repository;

import com.loopers.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(UUID orderId);

}
