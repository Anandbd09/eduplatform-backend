// FILE 12: SendOtpResponse.java
package com.eduplatform.otp.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SendOtpResponse {
    private String otpId;
    private String type; // SMS, EMAIL
    private String contactTarget;
    private Integer expiresInSeconds;
}