package com.loopers.domain.payment.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AttemptStep {

    REQUESTED(1),
    RESPONDED(2),
    FAILED(3);

    // -------------------------------------------------------------------------------------------------

    @JsonValue
    private final int code;

    @JsonCreator
    public static AttemptStep from(Integer value) {
        if (value == null) {
            return null;
        }

        for (AttemptStep step : values()) {
            if (step.code == value) {
                return step;
            }
        }

        return null;
    }

}
