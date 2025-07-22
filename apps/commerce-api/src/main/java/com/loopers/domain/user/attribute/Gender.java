package com.loopers.domain.user.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {

    MALE(1),
    FEMALE(2),
    OTHER(3);

    // -------------------------------------------------------------------------------------------------

    @JsonValue
    private final int code;

    @JsonCreator
    public static Gender from(Integer value) {
        if (value == null) {
            return null;
        }

        for (Gender gender : values()) {
            if (gender.code == value) {
                return gender;
            }
        }

        return null;
    }

}
