package com.loopers.domain.payment.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {

    POINT(1),
    CARD(2);

    // -------------------------------------------------------------------------------------------------

    @JsonValue
    private final int code;

    @JsonCreator
    public static PaymentMethod from(Integer value) {
        if (value == null) {
            return null;
        }

        for (PaymentMethod method : values()) {
            if (method.code == value) {
                return method;
            }
        }

        return null;
    }

}
