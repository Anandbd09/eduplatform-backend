// FILE 20: ExportTemplateResponse.java
package com.eduplatform.export.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ExportTemplateResponse {
    private String id;
    private String templateName;
    private String format;
    private LocalDateTime createdAt;
}