package com.eduplatform.gamification.dto;
import lombok.*;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AchievementResponse {
    private String id;
    private String title;
    private Boolean isUnlocked;
    private LocalDateTime unlockedAt;
    private Integer progress;
}
