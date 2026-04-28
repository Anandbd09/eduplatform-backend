// FILE 19: OtpVerifyRequest.java
package com.eduplatform.security.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OtpVerifyRequest {
    private String otp; // 6-digit code
    private String deviceId; // Optional
}