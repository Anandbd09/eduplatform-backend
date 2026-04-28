package com.eduplatform.gamification.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PointsResponse {
    private Long totalPoints;
    private Integer currentLevel;
    private Long pointsInCurrentLevel;
    private Long pointsToNextLevel;
    private String userRank;
    private Integer globalRank;
}