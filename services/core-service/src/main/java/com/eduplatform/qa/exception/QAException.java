package com.eduplatform.qa.exception;

public class QAException extends RuntimeException {

    private String code;
    private int httpStatus;

    public QAException(String message) {
        super(message);
        this.code = "QA_ERROR";
        this.httpStatus = 400;
    }

    public QAException(String message, String code) {
        super(message);
        this.code = code;
        this.httpStatus = 400;
    }

    public QAException(String message, String code, int httpStatus) {
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