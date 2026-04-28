// FILE 28: BatchReportResponse.java
package com.eduplatform.batch.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BatchReportResponse {
    private String jobId;
    private String jobType;
    private String status;
    private Integer totalRecords;
    private Integer processedRecords;
    private Integer successRecords;
    private Integer failedRecords;
    private String reportContent;
}