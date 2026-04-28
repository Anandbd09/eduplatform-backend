package com.eduplatform.reporting.exception;

public class ReportingException extends RuntimeException {

    private String code;
    private int httpStatus;

    public ReportingException(String message) {
        super(message);
        this.code = "REPORTING_ERROR";
        this.httpStatus = 400;
    }

    public ReportingException(String message, String code) {
        super(message);
        this.code = code;
        this.httpStatus = 400;
    }

    public ReportingException(String message, String code, int httpStatus) {
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