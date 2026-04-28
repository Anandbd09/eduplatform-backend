// FILE 16: ApplicationLogResponse.java
package com.eduplatform.monitoring.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ApplicationLogResponse {
    private String level;
    private String category;
    private String message;
    private String endpoint;
    private Long responseTimeMs;
    private Integer httpStatus;
    private LocalDateTime timestamp;
}