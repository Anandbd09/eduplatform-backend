package com.eduplatform.coupon.exception;

public class CouponException extends RuntimeException {

    private String code;
    private int httpStatus;

    public CouponException(String message) {
        super(message);
        this.code = "COUPON_ERROR";
        this.httpStatus = 400;
    }

    public CouponException(String message, String code) {
        super(message);
        this.code = code;
        this.httpStatus = 400;
    }

    public CouponException(String message, String code, int httpStatus) {
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