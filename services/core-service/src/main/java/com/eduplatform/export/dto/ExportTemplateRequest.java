// FILE 19: ExportTemplateRequest.java
package com.eduplatform.export.dto;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ExportTemplateRequest {
    private String templateName;
    private String description;
    private String sourceEntity;
    private String format;
    private List<String> selectedFields;
    private String filterCriteria;
    private Boolean isPublic;
}