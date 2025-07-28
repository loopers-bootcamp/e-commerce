package com.loopers.domain.payment.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

    COMPLETE(1),
    CANCELED(2);

    // -------------------------------------------------------------------------------------------------

    @JsonValue
    private final int code;

    @JsonCreator
    public static PaymentStatus from(Integer value) {
        if (value == null) {
            return null;
        }

        for (PaymentStatus status : values()) {
            if (status.code == value) {
                return status;
            }
        }

        return null;
    }

}
