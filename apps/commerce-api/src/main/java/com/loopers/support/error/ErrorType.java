package com.loopers.support.error;

import org.springframework.http.HttpStatus;

public interface ErrorType {

    HttpStatus getStatus();

    String getCode();

    String getMessage();

}
