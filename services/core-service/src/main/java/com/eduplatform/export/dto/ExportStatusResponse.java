// FILE 17: ExportStatusResponse.java
package com.eduplatform.export.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ExportStatusResponse {
    private String jobId;
    private String status;
    private Integer totalRecords;
    private Integer exportedRecords;
    private Integer failedRecords;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Long processingTimeMs;
    private String downloadUrl;
    private Boolean isExpired;
}