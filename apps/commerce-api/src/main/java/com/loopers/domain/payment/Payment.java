package com.loopers.domain.payment;

import com.loopers.config.jpa.converter.PaymentMethodConverter;
import com.loopers.config.jpa.converter.PaymentStatusConverter;
import com.loopers.domain.BaseEntity;
import com.loopers.domain.payment.attribute.PaymentMethod;
import com.loopers.domain.payment.attribute.PaymentStatus;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(name = "payments", indexes = {
        @Index(columnList = "ref_user_id, ref_order_id"),
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 결제 금액
     */
    @Column(name = "amount", nullable = false)
    private Long amount;

    /**
     * 결제 상태
     */
    @Convert(converter = PaymentStatusConverter.class)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    /**
     * 결제 수단
     */
    @Convert(converter = PaymentMethodConverter.class)
    @Column(name = "method", nullable = false)
    private PaymentMethod method;

    // -------------------------------------------------------------------------------------------------

    /**
     * 사용자 아이디
     */
    @Column(name = "ref_user_id", nullable = false, updatable = false)
    private Long userId;

    /**
     * 주문 아이디
     */
    @Column(name = "ref_order_id", nullable = false, updatable = false)
    private UUID orderId;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private Payment(
            Long amount,
            PaymentStatus status,
            PaymentMethod method,
            Long userId,
            UUID orderId
    ) {
        if (amount == null || amount < 0) {
            throw new BusinessException(CommonErrorType.INVALID, "결제 금액은 0 이상이어야 합니다.");
        }

        if (status == null) {
            throw new BusinessException(CommonErrorType.INVALID, "결제 상태가 올바르지 않습니다.");
        }

        if (method == null) {
            throw new BusinessException(CommonErrorType.INVALID, "결제 수단이 올바르지 않습니다.");
        }

        if (userId == null) {
            throw new BusinessException(CommonErrorType.INVALID, "사용자 아이디가 올바르지 않습니다.");
        }

        if (orderId == null) {
            throw new BusinessException(CommonErrorType.INVALID, "주문 아이디가 올바르지 않습니다.");
        }

        this.amount = amount;
        this.status = status;
        this.method = method;
        this.userId = userId;
        this.orderId = orderId;
    }

}
