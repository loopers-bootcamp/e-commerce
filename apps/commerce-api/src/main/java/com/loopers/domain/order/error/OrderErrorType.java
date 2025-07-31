package com.loopers.domain.order.error;

import com.loopers.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Locale;

@Getter
@RequiredArgsConstructor
public enum OrderErrorType implements ErrorType {

    CONCLUDING(HttpStatus.UNPROCESSABLE_ENTITY, "확정된 주문의 상태를 변경할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    OrderErrorType(HttpStatus status, String message) {
        this.status = status;
        this.code = "order:" + name().toLowerCase(Locale.ROOT);
        this.message = message;
    }

}
