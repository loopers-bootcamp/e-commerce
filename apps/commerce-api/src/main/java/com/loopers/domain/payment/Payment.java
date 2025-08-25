package com.loopers.domain.payment;

import com.loopers.config.jpa.converter.CardNumberConverter;
import com.loopers.config.jpa.converter.PaymentMethodConverter;
import com.loopers.config.jpa.converter.PaymentStatusConverter;
import com.loopers.domain.BaseEntity;
import com.loopers.domain.payment.attribute.CardNumber;
import com.loopers.domain.payment.attribute.CardType;
import com.loopers.domain.payment.attribute.PaymentMethod;
import com.loopers.domain.payment.attribute.PaymentStatus;
import com.loopers.domain.payment.error.PaymentErrorType;
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
@Table(
        name = "payments",
        indexes = {
                @Index(columnList = "ref_user_id, ref_order_id"),
        }
)
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

    /**
     * 카드 종류
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "card_type")
    private CardType cardType;

    /**
     * 카드 번호
     */
    @Convert(converter = CardNumberConverter.class)
    @Column(name = "card_number", length = 16)
    private CardNumber cardNumber;

    // -------------------------------------------------------------------------------------------------

    /**
     * 사용자 아이디
     */
    @Column(name = "ref_user_id", nullable = false, updatable = false)
    private Long userId;

    /**
     * 주문 아이디
     */
    @Column(name = "ref_order_id", nullable = false, updatable = false, unique = true)
    private UUID orderId;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private Payment(
            Long amount,
            PaymentStatus status,
            PaymentMethod method,
            CardType cardType,
            CardNumber cardNumber,
            Long userId,
            UUID orderId
    ) {
        if (amount == null || amount < 0) {
            throw new BusinessException(CommonErrorType.INVALID, "결제 금액은 0 이상이어야 합니다.");
        }

        if (status == null) {
            throw new BusinessException(CommonErrorType.INVALID, "결제 상태가 올바르지 않습니다.");
        }

        switch (method) {
            case POINT -> {
                if (cardType != null || cardNumber != null) {
                    throw new BusinessException(CommonErrorType.INVALID, "결제 수단에 해당하는 정보가 아닙니다.");
                }
            }
            case CARD -> {
                if (cardType == null) {
                    throw new BusinessException(CommonErrorType.INVALID, "카드 종류가 올바르지 않습니다.");
                }

                if (cardNumber == null) {
                    throw new BusinessException(CommonErrorType.INVALID, "카드 번호가 올바르지 않습니다.");
                }
            }
            case null -> throw new BusinessException(CommonErrorType.INVALID, "결제 수단이 올바르지 않습니다.");
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
        this.cardType = cardType;
        this.cardNumber = cardNumber;
        this.userId = userId;
        this.orderId = orderId;
    }

    public void pay() {
        if (this.status == PaymentStatus.PAID) {
            return;
        }

        if (this.status.isConcluding()) {
            throw new BusinessException(PaymentErrorType.ALREADY_CONCLUDED);
        }

        this.status = PaymentStatus.PAID;
    }

    public void fail() {
        if (this.status == PaymentStatus.FAILED) {
            return;
        }

        if (this.status.isConcluding()) {
            throw new BusinessException(PaymentErrorType.ALREADY_CONCLUDED);
        }

        this.status = PaymentStatus.FAILED;
    }

}
