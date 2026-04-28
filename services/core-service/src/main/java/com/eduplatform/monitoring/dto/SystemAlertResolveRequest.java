// FILE 23: SystemAlertResolveRequest.java
package com.eduplatform.monitoring.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SystemAlertResolveRequest {
    private String resolutionNotes;
}