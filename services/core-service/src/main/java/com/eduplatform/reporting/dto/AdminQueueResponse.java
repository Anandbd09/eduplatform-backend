// FILE 23: AdminQueueResponse.java
package com.eduplatform.reporting.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminQueueResponse {
    private Integer totalQueued;
    private Integer totalAssigned;
    private Integer totalOverdue;
    private Double averageDaysToResolve;
}