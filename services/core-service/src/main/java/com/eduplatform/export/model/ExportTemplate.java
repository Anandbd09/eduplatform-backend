package com.eduplatform.export.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "export_templates")
public class ExportTemplate {

    @Id
    private String id;

    @Indexed
    private String templateName;

    private String description;

    private String sourceEntity;

    @Indexed
    private String format;

    private List<String> selectedFields; // Which fields to include

    private String filterCriteria; // JSON

    private Integer displayOrder;

    @Indexed
    private Boolean isPublic; // Public or private template

    @Indexed
    private String createdBy;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private String tenantId;

    private Long version_field = 0L;
}