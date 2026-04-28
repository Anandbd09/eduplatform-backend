// FILE 20: DashboardMetricsResponse.java
package com.eduplatform.monitoring.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardMetricsResponse {
    private Long totalRequests;
    private Long errorCount;
    private Double errorRate;
    private Long averageResponseTimeMs;
    private Long activeAlerts;
    private Double uptime;
    private Long memoryUsedMb;
}