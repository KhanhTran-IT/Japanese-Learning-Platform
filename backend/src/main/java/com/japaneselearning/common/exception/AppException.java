package com.japaneselearning.common.exception;

import lombok.Getter;

/**
 * Custom application exception.
 * Thrown from service layer with a specific ErrorCode.
 * Caught and handled by GlobalExceptionHandler.
 */
@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}
