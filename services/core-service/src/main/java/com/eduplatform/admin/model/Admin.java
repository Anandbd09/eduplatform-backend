package com.eduplatform.admin.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "admins")
public class Admin {
    @Id
    private String id;

    @Indexed
    private String userId;

    private AdminLevel level; // SUPER_ADMIN, ADMIN, MODERATOR

    // Permissions
    private List<AdminPermission> permissions;

    // Activity
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Status
    private AdminStatus status; // ACTIVE, INACTIVE, SUSPENDED

    // Action tracking
    private Integer totalActionsPerformed;
    private LocalDateTime lastActionAt;

    @Indexed
    private String tenantId;
}
