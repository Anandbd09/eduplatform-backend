package com.eduplatform.notification.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "sms_templates")
public class SMSTemplate {
    @Id
    private String id;

    private String templateName;
    private String messageContent; // Max 160 characters

    private List<String> variables;

    private Boolean isActive;
    private Integer characterLimit; // For different countries

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}