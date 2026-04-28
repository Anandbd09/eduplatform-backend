// FILE 26: BatchResultResponse.java
package com.eduplatform.batch.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BatchResultResponse {
    private Integer recordNumber;
    private String recordIdentifier;
    private String status;
    private String message;
    private String errorCode;
    private String errorDetails;
    private LocalDateTime createdAt;
}