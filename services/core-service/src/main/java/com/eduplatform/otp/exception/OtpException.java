package com.eduplatform.otp.exception;

public class OtpException extends RuntimeException {
    private String code;
    private int httpStatus;

    public OtpException(String message) {
        super(message);
        this.code = "OTP_ERROR";
        this.httpStatus = 400;
    }

    public OtpException(String message, String code) {
        super(message);
        this.code = code;
        this.httpStatus = 400;
    }

    public OtpException(String message, String code, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() { return code; }
    public int getHttpStatus() { return httpStatus; }
}