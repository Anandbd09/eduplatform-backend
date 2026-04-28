// FILE 21: RecommendationAnalytics.java
package com.eduplatform.recommendation.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationAnalytics {
    private String userId;
    private Integer totalInteractions;
    private Integer viewCount;
    private Integer enrollCount;
    private Integer completeCount;
    private Integer rateCount;
    private Double avgEngagementScore;
    private LocalDateTime timestamp;
}