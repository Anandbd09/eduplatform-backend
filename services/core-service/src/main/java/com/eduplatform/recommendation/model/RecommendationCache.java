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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "recommendation_cache")
public class RecommendationCache {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private List<String> topCourseIds; // Top 100 recommended courses

    @Indexed
    private LocalDateTime cachedAt;

    @Indexed
    private LocalDateTime expiresAt; // 24 hours from cached date

    private Boolean isExpired = false;

    private String tenantId;

    /**
     * Check if cache is expired
     */
    public boolean isExpired() {
        return this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt);
    }
}