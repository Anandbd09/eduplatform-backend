// FILE 21: LogFilterRequest.java
package com.eduplatform.monitoring.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LogFilterRequest {
    private String level;
    private String category;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String userId;
}