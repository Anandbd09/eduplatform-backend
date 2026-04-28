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
@Document(collection = "course_similarities")
@CompoundIndex(name = "sourceCourseId_targetCourseId_idx",
        def = "{'sourceCourseId': 1, 'targetCourseId': 1}", unique = true)
public class CourseSimilarity {

    @Id
    private String id;

    @Indexed
    private String sourceCourseId;

    @Indexed
    private String targetCourseId;

    private Double similarityScore; // 0-1.0 (cosine similarity)

    @Indexed
    private LocalDateTime calculatedAt;

    private String tenantId;

    /**
     * Check if courses are similar (threshold: 0.7)
     */
    public boolean isSimilar() {
        return this.similarityScore != null && this.similarityScore >= 0.7;
    }
}