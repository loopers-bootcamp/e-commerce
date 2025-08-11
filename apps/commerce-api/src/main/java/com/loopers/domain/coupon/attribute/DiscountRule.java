package com.loopers.domain.coupon.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.function.ToIntBiFunction;

@Getter
@RequiredArgsConstructor
public enum DiscountRule {

    FIXED_AMOUNT(1, (a, b) -> b.intValue()),
    FIXED_RATE(2, (a, b) -> b.multiply(BigDecimal.valueOf(a)).intValue());

    // -------------------------------------------------------------------------------------------------

    @JsonValue
    private final int code;
    private final ToIntBiFunction<Long, BigDecimal> calculator;

    @JsonCreator
    public static DiscountRule from(Integer value) {
        if (value == null) {
            return null;
        }

        for (DiscountRule rule : values()) {
            if (rule.code == value) {
                return rule;
            }
        }

        return null;
    }

}
