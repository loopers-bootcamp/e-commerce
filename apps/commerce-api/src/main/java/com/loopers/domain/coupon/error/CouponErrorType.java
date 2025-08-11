package com.loopers.domain.coupon.error;

import com.loopers.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Locale;

@Getter
@RequiredArgsConstructor
public enum CouponErrorType implements ErrorType {

    REVOKED(HttpStatus.UNPROCESSABLE_ENTITY, "쿠폰이 폐기되었습니다."),
    NOT_ISSUABLE(HttpStatus.UNPROCESSABLE_ENTITY, "발급할 수 없는 쿠폰입니다."),
    UNAVAILABLE(HttpStatus.UNPROCESSABLE_ENTITY, "사용할 수 없는 쿠폰입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    CouponErrorType(HttpStatus status, String message) {
        this.status = status;
        this.code = "coupon:" + name().toLowerCase(Locale.ROOT);
        this.message = message;
    }

}
