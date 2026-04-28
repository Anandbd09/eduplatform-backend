// FILE 24: SecurityAuditResponse.java
package com.eduplatform.security.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SecurityAuditResponse {
    private String action;
    private String details;
    private String riskLevel;
    private String ipAddress;
    private LocalDateTime timestamp;
}