package com.loopers.domain.payment.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

    READY(1) {
        @Override
        public boolean isConcluding() {
            return false;
        }
    },

    PAID(2) {
        @Override
        public boolean isConcluding() {
            return true;
        }
    },

    FAILED(3) {
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

    public abstract boolean isConcluding();

}
