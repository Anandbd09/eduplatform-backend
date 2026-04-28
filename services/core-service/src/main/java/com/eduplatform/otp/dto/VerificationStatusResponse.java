// FILE 15: VerificationStatusResponse.java
package com.eduplatform.otp.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VerificationStatusResponse {
    private Boolean phoneVerified;
    private String phoneNumber;
    private LocalDateTime phoneVerifiedAt;
    private Boolean emailVerified;
    private String emailAddress;
    private LocalDateTime emailVerifiedAt;
}