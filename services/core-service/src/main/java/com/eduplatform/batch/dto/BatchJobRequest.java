// FILE 20: BatchJobRequest.java
package com.eduplatform.batch.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BatchJobRequest {
    private String jobType;
    private String sourceFormat;
}