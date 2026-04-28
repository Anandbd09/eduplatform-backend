// FILE 17: RecommendationResponse.java
package com.eduplatform.recommendation.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationResponse {
    private String id;
    private String courseId;
    private Double score;
    private String reason;
    private Boolean clicked;
    private LocalDateTime createdAt;
}