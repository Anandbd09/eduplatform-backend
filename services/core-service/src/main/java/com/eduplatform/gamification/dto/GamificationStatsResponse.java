package com.eduplatform.gamification.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class GamificationStatsResponse {
    private Long totalActiveStreaks;
    private Long totalPointsAwarded;
    private Long totalAchievementsUnlocked;
    private Integer averageLevel;
}
