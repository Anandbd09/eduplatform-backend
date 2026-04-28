// FILE 14: VerifyOtpResponse.java
package com.eduplatform.otp.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VerifyOtpResponse {
    private Boolean success;
    private String message;
    private String verificationToken;
    private LocalDateTime verifiedAt;
}