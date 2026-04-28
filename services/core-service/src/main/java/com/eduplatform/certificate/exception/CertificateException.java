package com.eduplatform.certificate.exception;

public class CertificateException extends RuntimeException {
    private String code;
    private int httpStatus;

    public CertificateException(String message) {
        super(message);
        this.code = "CERTIFICATE_ERROR";
        this.httpStatus = 400;
    }

    public CertificateException(String message, String code) {
        super(message);
        this.code = code;
        this.httpStatus = 400;
    }

    public CertificateException(String message, String code, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() { return code; }
    public int getHttpStatus() { return httpStatus; }
}