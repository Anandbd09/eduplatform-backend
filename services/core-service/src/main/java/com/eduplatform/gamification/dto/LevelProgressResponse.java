package com.eduplatform.gamification.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LevelProgressResponse {
    private Integer currentLevel;
    private Long pointsInCurrentLevel;
    private Long pointsToNextLevel;
    private Integer progressPercentage;
}