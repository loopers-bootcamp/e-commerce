package com.loopers.domain.point.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Cause {

    CHARGE(1, Direction.IN),
    PURCHASE(2, Direction.OUT);

    // -------------------------------------------------------------------------------------------------

    private final int code;
    private final Direction direction;

    @JsonCreator
    public static Cause from(Integer value) {
        if (value == null) {
            return null;
        }

        for (Cause cause : values()) {
            if (cause.code == value) {
                return cause;
            }
        }

        return null;
    }

}
