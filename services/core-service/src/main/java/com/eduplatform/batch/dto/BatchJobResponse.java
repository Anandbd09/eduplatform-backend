// FILE 21: BatchJobResponse.java
package com.eduplatform.batch.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BatchJobResponse {
    private String jobId;
    private String jobType;
    private String status;
    private String fileName;
    private Long fileSizeBytes;
    private Integer totalRecords;
    private Integer processedRecords;
    private Integer successRecords;
    private Integer failedRecords;
    private Integer skippedRecords;
    private Double progressPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}