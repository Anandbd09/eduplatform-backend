package com.eduplatform.common;

import java.time.LocalDateTime;

/**
 * Compatibility wrapper for older imports that still reference
 * com.eduplatform.common.ApiResponse.
 */
public class ApiResponse<T> extends com.eduplatform.core.common.response.ApiResponse<T> {

    public ApiResponse() {
        super();
    }

    public ApiResponse(boolean success, T data, String message, String error, String code, LocalDateTime timestamp) {
        super(success, data, message, error, code, timestamp);
    }

    public ApiResponse(boolean success, String message, T data) {
        super(success, data, message, null, null, LocalDateTime.now());
    }

    public ApiResponse(boolean success, String message, T data, String error, String code) {
        super(success, data, message, error, code, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Success");
    }

    public static <T> ApiResponse<T> error(String error, String code) {
        return new ApiResponse<>(false, null, null, error, code);
    }

    public static <T> ApiResponse<T> error(String error, String code, String message) {
        return new ApiResponse<>(false, message, null, error, code);
    }
}
