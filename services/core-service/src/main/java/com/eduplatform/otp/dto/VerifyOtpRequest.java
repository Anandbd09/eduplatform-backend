// FILE 13: VerifyOtpRequest.java
package com.eduplatform.otp.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VerifyOtpRequest {
    private String code; // 6-digit code
    private String purpose; // PHONE_VERIFICATION, EMAIL_VERIFICATION, PASSWORD_RESET
}