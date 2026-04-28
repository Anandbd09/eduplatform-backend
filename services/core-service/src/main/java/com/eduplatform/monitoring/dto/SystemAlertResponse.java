// FILE 19: SystemAlertResponse.java
package com.eduplatform.monitoring.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SystemAlertResponse {
    private String id;
    private String alertType;
    private String severity;
    private String title;
    private String status;
    private Double currentValue;
    private Double threshold;
    private LocalDateTime createdAt;
}