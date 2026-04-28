package com.eduplatform.export.exception;

public class ExportException extends RuntimeException {
    private String code;
    private int httpStatus;

    public ExportException(String message) {
        super(message);
        this.code = "EXPORT_ERROR";
        this.httpStatus = 400;
    }

    public ExportException(String message, String code) {
        super(message);
        this.code = code;
        this.httpStatus = 400;
    }

    public ExportException(String message, String code, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() { return code; }
    public int getHttpStatus() { return httpStatus; }
}