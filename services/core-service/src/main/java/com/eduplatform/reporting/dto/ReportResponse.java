// FILE 17: ReportResponse.java
package com.eduplatform.reporting.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {
    private String id;
    private String reportedEntityId;
    private String reportedEntityType;
    private String reporterId;
    private String reporterName;
    private String category;
    private String description;
    private String severity;
    private String status;
    private Integer evidenceCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}