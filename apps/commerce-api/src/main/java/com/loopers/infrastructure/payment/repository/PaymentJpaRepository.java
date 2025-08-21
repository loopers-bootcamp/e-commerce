package com.loopers.infrastructure.payment.repository;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.attribute.CardNumber;
import com.loopers.domain.payment.attribute.CardType;
import com.loopers.domain.payment.attribute.PaymentMethod;
import com.loopers.domain.payment.attribute.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(UUID orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.orderId = :orderId")
    Optional<Payment> findByOrderIdForUpdate(UUID orderId);

    @Modifying
    @Query("""
                insert into Payment (amount, status, method, cardType, cardNumber, userId, orderId, createdAt, updatedAt)
                values (:amount, :status, :method, :cardType, :cardNumber, :userId, :orderId, :createdAt, :updatedAt)
                on conflict (orderId) do nothing
            """)
    int insertIfNotExists(
            @Param("amount") Long amount,
            @Param("status") PaymentStatus status,
            @Param("method") PaymentMethod method,
            @Param("cardType") CardType cardType,
            @Param("cardNumber") CardNumber cardNumber,
            @Param("userId") Long userId,
            @Param("orderId") UUID orderId,
            @Param("createdAt") ZonedDateTime createdAt,
            @Param("updatedAt") ZonedDateTime updatedAt
    );

}
