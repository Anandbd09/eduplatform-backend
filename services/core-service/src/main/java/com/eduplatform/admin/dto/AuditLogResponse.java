package com.eduplatform.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditLogResponse {
    private String logId;
    private String adminId;
    private String adminName;
    private String action;
    private String targetType;
    private String targetId;
    private String targetName;
    private String status;
    private String description;
    private String reason;
    private String ipAddress;
    private LocalDateTime createdAt;
}