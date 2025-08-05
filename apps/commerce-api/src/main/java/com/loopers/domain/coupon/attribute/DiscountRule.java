package com.loopers.domain.coupon.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DiscountRule {

    FIXED_AMOUNT(1),
    FIXED_RATE(2);

    // -------------------------------------------------------------------------------------------------

    @JsonValue
    private final int code;

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
