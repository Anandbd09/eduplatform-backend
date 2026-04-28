package com.eduplatform.gamification.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "user_points")
public class UserPoints {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private Long totalPoints; // Cumulative across all courses

    private Integer currentLevel; // 1-100 (500 points per level)

    private Long pointsInCurrentLevel; // Progress to next level

    private String userRank; // BRONZE, SILVER, GOLD, PLATINUM, DIAMOND

    @Indexed
    private Integer globalRank; // Position in leaderboard

    private Integer courseRank; // Position in course leaderboard

    private Long monthlyPoints; // Points earned this month

    @Indexed
    private LocalDateTime lastPointsEarned;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private String tenantId;

    private Long version_field = 0L;
}