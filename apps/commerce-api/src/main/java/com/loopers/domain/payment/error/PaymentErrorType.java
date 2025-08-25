package com.loopers.domain.payment.error;

import com.loopers.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Locale;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorType implements ErrorType {

    UNPROCESSABLE(HttpStatus.UNPROCESSABLE_ENTITY, "결제할 수 없는 주문 건입니다."),
    ALREADY_CONCLUDED(HttpStatus.UNPROCESSABLE_ENTITY, "이미 종결된 결제입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    PaymentErrorType(HttpStatus status, String message) {
        this.status = status;
        this.code = "payment:" + name().toLowerCase(Locale.ROOT);
        this.message = message;
    }

}
