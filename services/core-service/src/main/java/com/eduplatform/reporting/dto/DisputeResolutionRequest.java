// FILE 20: DisputeResolutionRequest.java
package com.eduplatform.reporting.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputeResolutionRequest {
    private String decision;
    private String decisionReason;
    private String consequences;
    private String actionType;
    private Integer suspensionDays;
    private String resolvedByName;
    private String publicSummary;
}