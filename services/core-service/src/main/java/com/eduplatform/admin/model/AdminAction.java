package com.eduplatform.admin.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Document(collection = "admin_actions")
public class AdminAction {
    @Id
    private String id;

    @Indexed
    private String adminId;

    private String actionType;
    private String targetId;
    private String targetType;

    private String details;

    private ActionResult result;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private String tenantId;
}

enum ActionResult {
    SUCCESS,
    FAILURE,
    PARTIAL_SUCCESS
}