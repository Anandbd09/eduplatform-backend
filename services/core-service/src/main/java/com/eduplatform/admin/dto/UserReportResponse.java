package com.eduplatform.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserReportResponse {
    private String reportId;
    private String reportedUserId;
    private String reporterUserId;
    private String reason;
    private String description;
    private List<String> evidenceUrls;
    private String status;
    private String resolution;
    private String actionTaken;
    private LocalDateTime reportedAt;
    private LocalDateTime resolvedAt;
}