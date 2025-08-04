package com.loopers.domain.product.error;

import com.loopers.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Locale;

@Getter
@RequiredArgsConstructor
public enum ProductErrorType implements ErrorType {

    NOT_ENOUGH(HttpStatus.UNPROCESSABLE_ENTITY, "재고가 부족합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ProductErrorType(HttpStatus status, String message) {
        this.status = status;
        this.code = "product:" + name().toLowerCase(Locale.ROOT);
        this.message = message;
    }

}
