package com.loopers.domain.point.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Direction {

    IN(1),
    OUT(2);

    // -------------------------------------------------------------------------------------------------

    private final int value;

    @JsonCreator
    public static Direction from(int value) {
        for (Direction sex : values()) {
            if (sex.value == value) {
                return sex;
            }
        }

        return null;
    }

}
