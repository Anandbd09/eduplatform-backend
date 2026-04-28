package com.eduplatform.batch.exception;

public class BatchException extends RuntimeException {
    private String code;
    private int httpStatus;

    public BatchException(String message) {
        super(message);
        this.code = "BATCH_ERROR";
        this.httpStatus = 400;
    }

    public BatchException(String message, String code) {
        super(message);
        this.code = code;
        this.httpStatus = 400;
    }

    public BatchException(String message, String code, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() { return code; }
    public int getHttpStatus() { return httpStatus; }
}