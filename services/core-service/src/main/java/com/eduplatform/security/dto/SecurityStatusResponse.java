// FILE 20: SecurityStatusResponse.java
package com.eduplatform.security.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SecurityStatusResponse {
    private Boolean twoFactorEnabled;
    private String twoFactorMethod;
    private Long whitelistedIpsCount;
    private Long activeDevicesCount;
    private Long backupCodesRemaining;
}