package com.eduplatform.recommendation.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "course_vectors")
public class CourseVector {

    @Id
    private String id;

    @Indexed(unique = true)
    private String courseId;

    // Course Features for content-based filtering
    private String category;
    private String level; // BEGINNER, INTERMEDIATE, ADVANCED
    private Integer duration; // in hours
    private Double rating;
    private Double price;
    private String language;

    // Embeddings (vector representation)
    private List<Double> embedding; // sentence-transformer embeddings

    // Feature vector for similarity calculation
    private Map<String, Double> featureVector;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime updatedAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Build feature vector from course attributes
     */
    public void buildFeatureVector() {
        this.featureVector = new java.util.HashMap<>();

        // Normalize rating (0-1)
        if (this.rating != null) {
            this.featureVector.put("rating", this.rating / 5.0);
        }

        // Normalize price (0-1)
        if (this.price != null) {
            this.featureVector.put("price", Math.min(this.price / 500.0, 1.0));
        }

        // Duration feature (0-1)
        if (this.duration != null) {
            this.featureVector.put("duration", Math.min(this.duration / 100.0, 1.0));
        }

        // Category (encoded)
        if (this.category != null) {
            this.featureVector.put("category_" + this.category.toLowerCase(), 1.0);
        }

        // Level
        if (this.level != null) {
            switch (this.level) {
                case "BEGINNER" -> this.featureVector.put("level", 0.33);
                case "INTERMEDIATE" -> this.featureVector.put("level", 0.66);
                case "ADVANCED" -> this.featureVector.put("level", 1.0);
            }
        }
    }
}