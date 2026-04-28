package com.eduplatform.monitoring.exception;

public class MonitoringException extends RuntimeException {
    private String code;
    private int httpStatus;

    public MonitoringException(String message) {
        super(message);
        this.code = "MONITORING_ERROR";
        this.httpStatus = 400;
    }

    public MonitoringException(String message, String code) {
        super(message);
        this.code = code;
        this.httpStatus = 400;
    }

    public MonitoringException(String message, String code, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() { return code; }
    public int getHttpStatus() { return httpStatus; }
}