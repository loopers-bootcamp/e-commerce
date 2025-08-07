package com.loopers.domain.coupon.attribute;

import com.loopers.config.jpa.converter.DiscountRuleConverter;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Embeddable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiscountPolicy {

    /**
     * 할인 방법
     */
    @EqualsAndHashCode.Include
    @Convert(converter = DiscountRuleConverter.class)
    @Column(name = "discount_rule", nullable = false)
    private DiscountRule discountRule;

    /**
     * 할인 값
     */
    @EqualsAndHashCode.Include
    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    /**
     * 최대 할인 가능 금액
     */
    @EqualsAndHashCode.Include
    @Column(name = "max_discount_amount")
    private Integer maxDiscountAmount;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private DiscountPolicy(
            DiscountRule discountRule,
            BigDecimal discountValue,
            Integer maxDiscountAmount
    ) {
        if (discountRule == null) {
            throw new BusinessException(CommonErrorType.INVALID, "할인 방법이 올바르지 않습니다.");
        }

        if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(CommonErrorType.INVALID, "할인 값은 1 이상이어야 합니다.");
        }

        if (discountRule == DiscountRule.FIXED_AMOUNT && maxDiscountAmount == null) {
            maxDiscountAmount = discountValue.intValue();
        }

        if (maxDiscountAmount != null && maxDiscountAmount <= 0) {
            throw new BusinessException(CommonErrorType.INVALID, "최대 할인 가능 금액은 1 이상이어야 합니다.");
        }

        this.discountRule = discountRule;
        this.discountValue = discountValue;
        this.maxDiscountAmount = maxDiscountAmount;
    }

    public int calculateDiscountAmount(Long totalPrice) {
        return this.discountRule.getCalculator().applyAsInt(totalPrice, this.discountValue);
    }

}
