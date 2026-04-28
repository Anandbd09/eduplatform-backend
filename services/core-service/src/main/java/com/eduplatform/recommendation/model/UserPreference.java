package com.eduplatform.recommendation.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "user_preferences")
@CompoundIndex(name = "userId_courseId_idx", def = "{'userId': 1, 'courseId': 1, 'tenantId': 1}", unique = true)
public class UserPreference {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String courseId;

    private String interactionType; // VIEW, ENROLL, COMPLETE, RATE, CLICK

    private Double rating; // 1-5 if rated

    private Integer weight; // Importance: view=1, enroll=3, complete=5, rate=4

    private Double engagementScore; // 0-100 based on interaction

    @Indexed
    private LocalDateTime interactionDate;

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Calculate engagement score based on interaction type
     */
    public void calculateEngagementScore() {
        if (rating != null) {
            this.engagementScore = (rating / 5.0) * 100;
        } else {
            this.engagementScore = (this.weight / 5.0) * 100;
        }
    }
}