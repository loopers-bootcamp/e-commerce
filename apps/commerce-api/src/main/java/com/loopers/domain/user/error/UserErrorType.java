package com.loopers.domain.user.error;

import com.loopers.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorType implements ErrorType {

    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    UserErrorType(HttpStatus status, String message) {
        this.status = status;
        this.code = name();
        this.message = message;
    }

}
