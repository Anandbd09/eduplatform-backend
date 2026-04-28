// FILE 18: TwoFactorSetupResponse.java
package com.eduplatform.security.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TwoFactorSetupResponse {
    private String userId;
    private String method;
    private String secret; // Base32 encoded
    private String qrCode; // Data URL
    private Boolean requiresVerification;
}