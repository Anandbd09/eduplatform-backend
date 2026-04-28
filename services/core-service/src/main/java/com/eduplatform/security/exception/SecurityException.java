package com.eduplatform.security.exception;

public class SecurityException extends RuntimeException {
    private String code;
    private int httpStatus;

    public SecurityException(String message) {
        super(message);
        this.code = "SECURITY_ERROR";
        this.httpStatus = 400;
    }

    public SecurityException(String message, String code) {
        super(message);
        this.code = code;
        this.httpStatus = 400;
    }

    public SecurityException(String message, String code, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() { return code; }
    public int getHttpStatus() { return httpStatus; }
}