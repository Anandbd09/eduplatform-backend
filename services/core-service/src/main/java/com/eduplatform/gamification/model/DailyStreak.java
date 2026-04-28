package com.eduplatform.gamification.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "daily_streaks")
public class DailyStreak {

    @Id
    private String id;

    @Indexed(unique = true)
    private String streakKey; // userId:courseId

    @Indexed
    private String userId;

    @Indexed
    private String courseId;

    private Integer currentStreak; // Days in a row

    private Integer longestStreak; // All-time best

    @Indexed
    private LocalDate lastActivityDate; // Last day they participated

    @Indexed
    private LocalDate streakStartDate; // When current streak began

    private LocalDate longestStreakStartDate;

    private LocalDate longestStreakEndDate;

    private Integer totalLessonsCompleted; // In this course

    private Integer totalMinutesSpent; // On this course

    private Boolean isActive; // Current streak alive

    @Indexed
    private LocalDate createdAt;

    private LocalDate updatedAt;

    @Indexed
    private String tenantId;

    private Long version_field = 0L;

    /**
     * Is streak alive today
     */
    public boolean isStreakAlive() {
        if (lastActivityDate == null) return false;
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        return lastActivityDate.equals(today) || lastActivityDate.equals(yesterday);
    }
}