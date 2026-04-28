package com.eduplatform.gamification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreakResponse {
    private Integer currentStreak;
    private Integer longestStreak;
    private Integer totalLessonsCompleted;
    private Boolean isStreakAlive;
}