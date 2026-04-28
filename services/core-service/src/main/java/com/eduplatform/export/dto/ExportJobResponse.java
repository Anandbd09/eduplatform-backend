// FILE 16: ExportJobResponse.java
package com.eduplatform.export.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ExportJobResponse {
    private String jobId;
    private String exportType;
    private String format;
    private String status;
    private LocalDateTime createdAt;
    private String downloadUrl;
}