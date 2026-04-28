package com.eduplatform.wishlist.exception;

public class WishlistException extends RuntimeException {

    private String code;
    private int httpStatus;

    public WishlistException(String message) {
        super(message);
        this.code = "WISHLIST_ERROR";
        this.httpStatus = 400;
    }

    public WishlistException(String message, String code) {
        super(message);
        this.code = code;
        this.httpStatus = 400;
    }

    public WishlistException(String message, String code, int httpStatus) {
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