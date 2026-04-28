package com.eduplatform.security.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "security_audit_logs")
public class SecurityAuditLog {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String action; // 2FA_ENABLED, 2FA_DISABLED, LOGIN_SUCCESS, LOGIN_FAILED, etc

    private String details;

    private String ipAddress;

    private String userAgent;

    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL

    private Boolean requiresApproval;

    private Boolean isApproved;

    @Indexed
    private LocalDateTime timestamp;

    @Indexed
    private String tenantId;

    private Long version = 0L;
}