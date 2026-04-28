// FILE 21: DisputeResolutionResponse.java
package com.eduplatform.reporting.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputeResolutionResponse {
    private String id;
    private String disputeId;
    private String decision;
    private String decisionReason;
    private String consequences;
    private String actionType;
    private Boolean appealed;
    private Long appealDaysRemaining;
    private LocalDateTime resolvedAt;
    private String publicSummary;
}