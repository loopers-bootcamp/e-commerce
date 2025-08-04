package com.loopers.support.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Locale;

@Getter
@RequiredArgsConstructor
public enum CommonErrorType implements ErrorType {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID(HttpStatus.BAD_REQUEST, "올바르지 않은 값입니다."),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "인증되지 않았습니다."),
    UNAUTHORIZED(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 리소스입니다."),
    CONFLICT(HttpStatus.CONFLICT, "이미 존재하는 리소스입니다."),
    INCONSISTENT(HttpStatus.UNPROCESSABLE_ENTITY, "일관된 리소스가 아닙니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "일시적인 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    CommonErrorType(HttpStatus status, String message) {
        this.status = status;
        this.code = "common:" + name().toLowerCase(Locale.ROOT);
        this.message = message;
    }

}
