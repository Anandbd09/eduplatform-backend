// FILE 17: TwoFactorSetupRequest.java
package com.eduplatform.security.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TwoFactorSetupRequest {
    private String method; // TOTP, SMS, EMAIL
    private String phoneNumber; // For SMS
}