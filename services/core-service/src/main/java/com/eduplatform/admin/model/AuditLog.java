package com.eduplatform.admin.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = "audit_logs")
@CompoundIndex(name = "admin_action_idx", def = "{'adminId': 1, 'createdAt': -1}")
public class AuditLog {
    @Id
    private String id;

    @Indexed
    private String adminId;
    private String adminName;

    private AuditAction action; // USER_BANNED, COURSE_APPROVED, etc

    private String targetType; // USER, COURSE, PAYMENT, etc
    @Indexed
    private String targetId;
    private String targetName;

    @Indexed
    private AuditStatus status; // SUCCESS, FAILED, PENDING

    private String description;
    private String reason;

    // Changes
    private Map<String, Object> changesBefore;
    private Map<String, Object> changesAfter;

    // Client info
    private String ipAddress;
    private String userAgent;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private String tenantId;
}
