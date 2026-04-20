package com.eduplatform.core.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public AppException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public static AppException notFound(String message) {
        return new AppException(message, "NOT_FOUND", HttpStatus.NOT_FOUND);
    }

    public static AppException badRequest(String message) {
        return new AppException(message, "BAD_REQUEST", HttpStatus.BAD_REQUEST);
    }

    public static AppException unauthorized(String message) {
        return new AppException(message, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
    }

    public static AppException conflict(String message) {
        return new AppException(message, "CONFLICT", HttpStatus.CONFLICT);
    }

    public static AppException internalError(String message) {
        return new AppException(message, "INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}