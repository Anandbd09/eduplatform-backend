// FILE 18: SystemAlertRequest.java
package com.eduplatform.monitoring.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SystemAlertRequest {
    private String alertType;
    private String severity;
    private String title;
    private String description;
}