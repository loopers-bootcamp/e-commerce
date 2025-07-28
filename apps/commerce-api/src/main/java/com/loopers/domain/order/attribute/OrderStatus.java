package com.loopers.domain.order.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    CREATED(1),
    COMPLETE(2),
    EXPIRED(3),
    CANCELED(4);

    // -------------------------------------------------------------------------------------------------

    @JsonValue
    private final int code;

    @JsonCreator
    public static OrderStatus from(Integer value) {
        if (value == null) {
            return null;
        }

        for (OrderStatus status : values()) {
            if (status.code == value) {
                return status;
            }
        }

        return null;
    }

}
