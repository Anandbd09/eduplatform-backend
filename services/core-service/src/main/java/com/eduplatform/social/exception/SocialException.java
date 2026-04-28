package com.eduplatform.social.exception;

public class SocialException extends RuntimeException {
    private String code;
    private int httpStatus;

    public SocialException(String message) {
        super(message);
        this.code = "SOCIAL_ERROR";
        this.httpStatus = 400;
    }

    public SocialException(String message, String code) {
        super(message);
        this.code = code;
        this.httpStatus = 400;
    }

    public SocialException(String message, String code, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() { return code; }
    public int getHttpStatus() { return httpStatus; }
}