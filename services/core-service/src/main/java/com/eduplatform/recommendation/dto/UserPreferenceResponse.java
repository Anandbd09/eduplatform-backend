// FILE 16: UserPreferenceResponse.java
package com.eduplatform.recommendation.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferenceResponse {
    private String id;
    private String courseId;
    private String interactionType;
    private Double rating;
    private Double engagementScore;
    private LocalDateTime interactionDate;
}