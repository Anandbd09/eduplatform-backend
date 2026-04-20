package com.eduplatform.notification.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "email_templates")
public class EmailTemplate {
    @Id
    private String id;

    @Indexed
    private String templateName;

    private String subject;
    private String htmlContent;
    private String textContent;

    private String description;

    // Template variables: {{firstName}}, {{courseName}}, etc
    private List<String> variables;

    // Email configuration
    private String fromEmail;
    private String fromName;
    private List<String> replyToEmails;

    // Flags
    private Boolean isActive;
    private Boolean isDefault;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Indexed
    private String tenantId;
}