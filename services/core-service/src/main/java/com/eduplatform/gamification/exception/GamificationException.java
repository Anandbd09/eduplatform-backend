package com.eduplatform.gamification.exception;

public class GamificationException extends RuntimeException {
    private String code;
    private int httpStatus;

    public GamificationException(String message) {
        super(message);
        this.code = "GAMIFICATION_ERROR";
        this.httpStatus = 400;
    }

    public String getCode() { return code; }
    public int getHttpStatus() { return httpStatus; }
}