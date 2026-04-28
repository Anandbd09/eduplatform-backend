// FILE 22: MonitoringHealthResponse.java
package com.eduplatform.monitoring.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MonitoringHealthResponse {
    private String status;
    private String message;
    private Double uptime;
}