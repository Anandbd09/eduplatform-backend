package com.eduplatform.core.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String message;
    private String error;
    private String code;
    private LocalDateTime timestamp;

    /**
     * Success response
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Success response with default message
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Success");
    }

    /**
     * Error response
     */
    public static <T> ApiResponse<T> error(String error, String code) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .code(code)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Error response with message
     */
    public static <T> ApiResponse<T> error(String error, String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}