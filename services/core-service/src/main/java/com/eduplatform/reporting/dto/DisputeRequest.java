// FILE 18: DisputeRequest.java
package com.eduplatform.reporting.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputeRequest {
    private String reportId;
    private String disputedUserId;
    private String disputedUserName;
    private String disputedUserEmail;
    private String reason;
}