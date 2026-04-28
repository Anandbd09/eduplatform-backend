// FILE 22: ExportAuditResponse.java
package com.eduplatform.export.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ExportAuditResponse {
    private String jobId;
    private String userId;
    private String action;
    private String details;
    private LocalDateTime timestamp;
}