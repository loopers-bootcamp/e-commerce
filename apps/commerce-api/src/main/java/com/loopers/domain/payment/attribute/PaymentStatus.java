package com.loopers.domain.payment.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

    READY("ready") {
        @Override
        public boolean isConcluding() {
            return false;
        }
    },

    PENDING("pending") {
        @Override
        public boolean isConcluding() {
            return false;
        }
    },

    PAID("paid") {
        @Override
        public boolean isConcluding() {
            return true;
        }
    },

    FAILED("failed") {
        @Override
        public boolean isConcluding() {
            return true;
        }
    },

    CANCELED("canceled") {
        @Override
        public boolean isConcluding() {
            return true;
        }
    };

    // -------------------------------------------------------------------------------------------------

    @JsonValue
    private final String code;

    @JsonCreator
    public static PaymentStatus from(String value) {
        if (value == null) {
            return null;
        }

        for (PaymentStatus status : values()) {
            if (status.code.equals(value)) {
                return status;
            }
        }

        return null;
    }

    public abstract boolean isConcluding();

}
