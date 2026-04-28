package com.eduplatform.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminResponse {
    private String adminId;
    private String userId;
    private String adminLevel;
    private List<String> permissions;
    private String status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private Integer totalActionsPerformed;
}