package com.eduplatform.admin.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "user_reports")
public class UserReport {
    @Id
    private String id;

    @Indexed
    private String reportedUserId;
    private String reporterUserId;

    private UserReportReason reason; // HARASSMENT, FRAUD, SPAM, etc

    private String description;
    private List<String> evidenceUrls;

    @Indexed
    private ReportStatus status; // OPEN, UNDER_REVIEW, RESOLVED, DISMISSED

    // Resolution
    private String resolvedBy;
    private String resolution;
    private UserReportAction actionTaken; // WARNED, SUSPENDED, BANNED, NONE

    private LocalDateTime reportedAt;
    private LocalDateTime resolvedAt;

    @Indexed
    private String tenantId;
}

enum UserReportReason {
    HARASSMENT,
    FRAUD,
    SPAM,
    INAPPROPRIATE_CONTENT,
    INTELLECTUAL_PROPERTY,
    OTHER
}

enum ReportStatus {
    OPEN,
    UNDER_REVIEW,
    RESOLVED,
    DISMISSED
}

enum UserReportAction {
    WARNED,
    SUSPENDED,
    BANNED,
    CONTENT_REMOVED,
    NONE
}