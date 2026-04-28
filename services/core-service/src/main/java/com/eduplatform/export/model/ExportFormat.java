package com.eduplatform.export.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "export_formats")
public class ExportFormat {

    @Id
    private String id;

    private String formatName; // CSV, PDF, EXCEL, JSON

    private String mimeType;

    private String fileExtension;

    private Integer maxRecords;

    private Boolean supportsFiltering;

    private Boolean supportsEncryption;

    private String description;

    private Long version_field = 0L;
}