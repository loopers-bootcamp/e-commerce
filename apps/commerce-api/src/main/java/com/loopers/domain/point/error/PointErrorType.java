package com.loopers.domain.point.error;

import com.loopers.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Locale;

@Getter
@RequiredArgsConstructor
public enum PointErrorType implements ErrorType {

    EXCESSIVE(HttpStatus.UNPROCESSABLE_ENTITY, "포인트의 최대치를 초과하였습니다."),
    NOT_ENOUGH(HttpStatus.UNPROCESSABLE_ENTITY, "포인트가 부족합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    PointErrorType(HttpStatus status, String message) {
        this.status = status;
        this.code = "point:" + name().toLowerCase(Locale.ROOT);
        this.message = message;
    }

}
