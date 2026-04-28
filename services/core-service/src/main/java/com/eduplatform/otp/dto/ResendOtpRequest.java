// FILE 16: ResendOtpRequest.java
package com.eduplatform.otp.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ResendOtpRequest {
    private String purpose; // PHONE_VERIFICATION, EMAIL_VERIFICATION, PASSWORD_RESET
}