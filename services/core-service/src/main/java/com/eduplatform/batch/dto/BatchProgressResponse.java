// FILE 29: BatchProgressResponse.java
package com.eduplatform.batch.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BatchProgressResponse {
    private String jobId;
    private String status;
    private Integer processedRecords;
    private Integer totalRecords;
    private Double progressPercentage;
    private Long estimatedSecondsRemaining;
}