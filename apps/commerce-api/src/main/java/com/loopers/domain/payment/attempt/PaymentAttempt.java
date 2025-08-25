package com.loopers.domain.payment.attempt;

import com.loopers.config.jpa.converter.AttemptStepConverter;
import com.loopers.domain.BaseEntity;
import com.loopers.domain.payment.attribute.AttemptStep;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Event Sourcing Pattern
 */
@Getter
@Entity
@Table(
        name = "payment_attempts",
        indexes = {
                @Index(columnList = "ref_payment_id"),
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentAttempt extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_attempt_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 거래 키
     */
    @Column(name = "transaction_key")
    private String transactionKey;

    /**
     * 결제 요청 단계
     */
    @Convert(converter = AttemptStepConverter.class)
    @Column(name = "step", nullable = false, updatable = false)
    private AttemptStep step;

    /**
     * 실패 사유
     */
    @Column(name = "fail_reason")
    private String failReason;

    // -------------------------------------------------------------------------------------------------

    /**
     * 주문 아이디
     */
    @Column(name = "ref_order_id", nullable = false, updatable = false)
    private UUID orderId;

    /**
     * 결제 아이디
     */
    @Column(name = "ref_payment_id", nullable = false, updatable = false)
    private Long paymentId;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private PaymentAttempt(
            AttemptStep step,
            String transactionKey,
            String failReason,
            UUID orderId,
            Long paymentId
    ) {
        if (step == null) {
            throw new BusinessException(CommonErrorType.INVALID, "결제 요청 단계가 올바르지 않습니다.");
        }

        if (transactionKey != null && !StringUtils.hasText(transactionKey)) {
            throw new BusinessException(CommonErrorType.INVALID, "거래 키가 올바르지 않습니다.");
        }

        if (failReason != null && !StringUtils.hasText(failReason)) {
            throw new BusinessException(CommonErrorType.INVALID, "실패 사유가 올바르지 않습니다.");
        }

        if (orderId == null) {
            throw new BusinessException(CommonErrorType.INVALID, "주문 아이디가 올바르지 않습니다.");
        }

        if (paymentId == null) {
            throw new BusinessException(CommonErrorType.INVALID, "결제 아이디가 올바르지 않습니다.");
        }

        this.step = step;
        this.transactionKey = transactionKey;
        this.failReason = failReason;
        this.orderId = orderId;
        this.paymentId = paymentId;
    }

}
