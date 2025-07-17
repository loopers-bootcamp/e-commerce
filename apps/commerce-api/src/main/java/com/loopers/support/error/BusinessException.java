package com.loopers.support.error;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorType errorType;
    private final String customMessage;

    public BusinessException(ErrorType errorType) {
        this(errorType, null);
    }

    public BusinessException(ErrorType errorType, String customMessage) {
        super(customMessage != null ? customMessage : errorType.getMessage());
        this.errorType = errorType;
        this.customMessage = customMessage;
    }

}
