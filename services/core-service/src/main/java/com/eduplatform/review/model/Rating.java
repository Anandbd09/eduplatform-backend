package com.eduplatform.review.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "ratings")
public class Rating {

    @Id
    private String id;

    @Indexed(unique = true)
    private String courseId;

    private Double averageRating = 0.0;
    private Integer totalRatings = 0;

    private Integer oneStarCount = 0;
    private Integer twoStarCount = 0;
    private Integer threeStarCount = 0;
    private Integer fourStarCount = 0;
    private Integer fiveStarCount = 0;

    @Indexed
    private LocalDateTime lastUpdated;

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Calculate percentage of reviews for a specific star rating
     */
    public Double getPercentage(int star) {
        if (totalRatings == 0) {
            return 0.0;
        }

        int count = switch (star) {
            case 1 -> oneStarCount;
            case 2 -> twoStarCount;
            case 3 -> threeStarCount;
            case 4 -> fourStarCount;
            case 5 -> fiveStarCount;
            default -> 0;
        };

        return (double) (count * 100) / totalRatings;
    }

    /**
     * Get rating distribution as array [1-star%, 2-star%, etc]
     */
    public Double[] getDistribution() {
        return new Double[]{
                getPercentage(1),
                getPercentage(2),
                getPercentage(3),
                getPercentage(4),
                getPercentage(5)
        };
    }

    /**
     * Format rating for display (e.g., "4.5 out of 5")
     */
    public String getFormattedRating() {
        return String.format("%.1f out of 5 (%d reviews)", averageRating, totalRatings);
    }
}