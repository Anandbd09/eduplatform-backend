// FILE 17: PerformanceMetricResponse.java
package com.eduplatform.monitoring.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PerformanceMetricResponse {
    private String endpoint;
    private Long responseTimeMs;
    private Long memoryUsedMb;
    private Double cpuUsagePercent;
    private Long requestsPerMinute;
    private LocalDateTime timestamp;
}