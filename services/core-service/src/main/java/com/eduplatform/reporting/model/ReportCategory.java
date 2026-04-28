package com.eduplatform.reporting.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "report_categories")
public class ReportCategory {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code; // PLAGIARISM, FRAUD, etc

    private String name;

    private String description;

    private Integer defaultPriority; // 1-10

    private String defaultSeverity; // LOW, MEDIUM, HIGH, CRITICAL

    private Boolean requiresEvidence = false;

    private String tenantId;

    private Long version = 0L;
}