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
@Document(collection = "user_achievements")
public class UserAchievement {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String achievementId;

    @Indexed
    private Boolean isUnlocked; // Has user earned this

    @Indexed
    private LocalDateTime unlockedAt;

    private Integer progress; // % complete (0-100)

    @Indexed
    private String tenantId;

    private Long version_field = 0L;
}