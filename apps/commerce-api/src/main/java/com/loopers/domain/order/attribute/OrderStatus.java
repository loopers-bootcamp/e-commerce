package com.loopers.domain.order.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    CREATED(1) {
        @Override
        public boolean isConcluding() {
            return false;
        }
    },

    COMPLETE(2) {
        @Override
        public boolean isConcluding() {
            return true;
        }
    },

    EXPIRED(3) {
        @Override
        public boolean isConcluding() {
            return true;
        }
    },

    CANCELED(4) {
        @Override
        public boolean isConcluding() {
            return true;
        }
    };

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

    public abstract boolean isConcluding();

    public boolean isPayable() {
        return !isConcluding();
    }

}
