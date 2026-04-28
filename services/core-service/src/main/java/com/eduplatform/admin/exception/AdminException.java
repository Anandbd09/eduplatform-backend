package com.eduplatform.admin.exception;

public class AdminException extends RuntimeException {

    private String errorCode;

    public AdminException(String message) {
        super(message);
    }

    public AdminException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AdminException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getErrorCode() {
        return errorCode;
    }
}