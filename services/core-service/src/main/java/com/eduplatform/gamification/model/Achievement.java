package com.eduplatform.gamification.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "achievements")
public class Achievement {

    @Id
    private String id;

    @Indexed
    private String achievementCode; // FIRST_LESSON, 7_DAY_STREAK, PERFECT_SCORE

    private String title;

    private String description;

    private String icon; // URL to badge icon

    private String category; // LEARNING, STREAK, SOCIAL, CHALLENGE

    private Integer pointsReward; // Points earned

    @Indexed
    private String tenantId;

    private Long version_field = 0L;
}