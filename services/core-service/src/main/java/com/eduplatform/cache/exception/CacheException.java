package com.eduplatform.cache.exception;

public class CacheException extends RuntimeException {
    private String code;
    private int httpStatus;

    public CacheException(String message) {
        super(message);
        this.code = "CACHE_ERROR";
        this.httpStatus = 400;
    }

    public CacheException(String message, String code) {
        super(message);
        this.code = code;
        this.httpStatus = 400;
    }

    public CacheException(String message, String code, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() { return code; }
    public int getHttpStatus() { return httpStatus; }
}