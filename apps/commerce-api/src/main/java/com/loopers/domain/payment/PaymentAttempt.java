package com.loopers.domain.payment;

import com.loopers.config.jpa.converter.AttemptStepConverter;
import com.loopers.domain.BaseEntity;
import com.loopers.domain.payment.attribute.AttemptStep;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
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
     * 결제 요청 번호
     */
    @Column(name = "merchant_uid", nullable = false, updatable = false)
    private UUID merchantUid;

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

    // -------------------------------------------------------------------------------------------------

    /**
     * 결제 아이디
     */
    @Column(name = "ref_payment_id", nullable = false, updatable = false)
    private Long paymentId;

    // -------------------------------------------------------------------------------------------------

    private PaymentAttempt(
            UUID merchantUid,
            String transactionKey,
            AttemptStep step,
            Long paymentId
    ) {
        if (merchantUid == null) {
            throw new BusinessException(CommonErrorType.INVALID, "결제 요청 번호가 올바르지 않습니다.");
        }

        if (transactionKey != null && !StringUtils.hasText(transactionKey)) {
            throw new BusinessException(CommonErrorType.INVALID, "거래 키가 올바르지 않습니다.");
        }

        if (paymentId == null) {
            throw new BusinessException(CommonErrorType.INVALID, "결제 아이디가 올바르지 않습니다.");
        }

        this.merchantUid = merchantUid;
        this.transactionKey = transactionKey;
        this.step = step;
        this.paymentId = paymentId;
    }

    public static PaymentAttempt request(Long paymentId) {
        return new PaymentAttempt(UUID.randomUUID(), null, AttemptStep.REQUESTED, paymentId);
    }

    public static PaymentAttempt respond(UUID merchantUid, String transactionKey, Long paymentId) {
        return new PaymentAttempt(merchantUid, transactionKey, AttemptStep.RESPONDED, paymentId);
    }

    public static PaymentAttempt fail(UUID merchantUid, Long paymentId) {
        return new PaymentAttempt(merchantUid, null, AttemptStep.FAILED, paymentId);
    }

}
