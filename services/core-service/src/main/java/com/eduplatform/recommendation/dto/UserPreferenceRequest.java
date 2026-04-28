// FILE 15: UserPreferenceRequest.java
package com.eduplatform.recommendation.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferenceRequest {
    private String courseId;
    private String interactionType; // VIEW, ENROLL, COMPLETE, RATE, CLICK
    private Double rating;
}