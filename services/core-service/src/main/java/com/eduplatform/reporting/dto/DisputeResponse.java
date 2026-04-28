// FILE 19: DisputeResponse.java
package com.eduplatform.reporting.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputeResponse {
    private String id;
    private String reportId;
    private String disputedUserId;
    private String disputedUserName;
    private Integer priority;
    private String status;
    private String assignedTo;
    private String assignedToName;
    private LocalDateTime responseDeadline;
    private Long daysRemaining;
    private LocalDateTime createdAt;
}