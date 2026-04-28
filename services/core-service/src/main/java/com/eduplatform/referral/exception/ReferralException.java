package com.eduplatform.referral.exception;

public class ReferralException extends RuntimeException {
    private String code;
    private int httpStatus;

    public ReferralException(String message) {
        super(message);
        this.code = "REFERRAL_ERROR";
        this.httpStatus = 400;
    }

    public ReferralException(String message, String code) {
        super(message);
        this.code = code;
        this.httpStatus = 400;
    }

    public ReferralException(String message, String code, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() { return code; }
    public int getHttpStatus() { return httpStatus; }
}