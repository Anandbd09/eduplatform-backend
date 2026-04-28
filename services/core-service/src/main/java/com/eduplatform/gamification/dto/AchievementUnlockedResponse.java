package com.eduplatform.gamification.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AchievementUnlockedResponse {
    private String achievementCode;
    private String title;
    private Integer pointsAwarded;
    private LocalDateTime unlockedAt;
}