// FILE 11: SendOtpRequest.java
package com.eduplatform.otp.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SendOtpRequest {
    private String phoneNumber;
    private String emailAddress;
    private String purpose; // PHONE_VERIFICATION, EMAIL_VERIFICATION, PASSWORD_RESET
}