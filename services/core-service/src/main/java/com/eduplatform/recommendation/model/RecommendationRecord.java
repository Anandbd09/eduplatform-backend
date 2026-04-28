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
@Document(collection = "recommendations")
@CompoundIndex(name = "userId_courseId_idx", def = "{'userId': 1, 'courseId': 1, 'tenantId': 1}")
public class RecommendationRecord {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String courseId;

    private Double score; // 0-100

    private String reason; // COLLABORATIVE, CONTENT, TRENDING, SIMILAR

    private Double collaborativeScore; // 0-100

    private Double contentScore; // 0-100

    private Boolean clicked = false;

    @Indexed
    private LocalDateTime createdAt;

    private LocalDateTime clickedAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Calculate hybrid score (50% collab + 50% content)
     */
    public void calculateHybridScore() {
        if (this.collaborativeScore != null && this.contentScore != null) {
            this.score = (this.collaborativeScore * 0.5) + (this.contentScore * 0.5);
        } else if (this.collaborativeScore != null) {
            this.score = this.collaborativeScore;
        } else if (this.contentScore != null) {
            this.score = this.contentScore;
        }
    }

    /**
     * Mark as clicked
     */
    public void markClicked() {
        this.clicked = true;
        this.clickedAt = LocalDateTime.now();
    }
}