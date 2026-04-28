package com.eduplatform.review.exception;

public class ReviewException extends RuntimeException {

    private String code;
    private int httpStatus;

    public ReviewException(String message) {
        super(message);
        this.code = "REVIEW_ERROR";
        this.httpStatus = 400;
    }

    public ReviewException(String message, String code) {
        super(message);
        this.code = code;
        this.httpStatus = 400;
    }

    public ReviewException(String message, String code, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}