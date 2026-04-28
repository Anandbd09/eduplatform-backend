package com.eduplatform.video.exception;

public class VideoException extends RuntimeException {
    private String code;
    private int httpStatus;

    public VideoException(String message) {
        super(message);
        this.code = "VIDEO_ERROR";
        this.httpStatus = 400;
    }

    public VideoException(String message, String code) {
        super(message);
        this.code = code;
        this.httpStatus = 400;
    }

    public VideoException(String message, String code, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() { return code; }
    public int getHttpStatus() { return httpStatus; }
}