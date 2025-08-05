package com.loopers.domain.coupon.attribute;

import com.loopers.config.jpa.converter.DiscountRuleConverter;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiscountPolicy {

    /**
     * 할인 방법
     */
    @Convert(converter = DiscountRuleConverter.class)
    @Column(name = "discount_rule", nullable = false)
    private DiscountRule discountRule;

    /**
     * 할인 값
     */
    @Column(name = "discount_value", nullable = false)
    private Integer discountValue;

    /**
     * 최대 할인 가능 금액
     */
    @Column(name = "max_discount_amount")
    private Integer maxDiscountAmount;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private DiscountPolicy(
            DiscountRule discountRule,
            Integer discountValue,
            Integer maxDiscountAmount
    ) {
        if (discountRule == null) {
            throw new BusinessException(CommonErrorType.INVALID, "할인 방법이 올바르지 않습니다.");
        }

        if (discountValue == null || discountValue <= 0) {
            throw new BusinessException(CommonErrorType.INVALID, "할인 값은 1 이상이어야 합니다.");
        }

        if (maxDiscountAmount != null && maxDiscountAmount <= 0) {
            throw new BusinessException(CommonErrorType.INVALID, "최대 할인 가능 금액은 1 이상이어야 합니다.");
        }

        this.discountRule = discountRule;
        this.discountValue = discountValue;
        this.maxDiscountAmount = maxDiscountAmount;
    }

}
