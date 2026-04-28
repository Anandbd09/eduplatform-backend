package com.eduplatform.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingStats {

    private Double averageRating;
    private Integer totalRatings;

    private Integer oneStarCount;
    private Integer twoStarCount;
    private Integer threeStarCount;
    private Integer fourStarCount;
    private Integer fiveStarCount;

    private Double oneStarPercentage;
    private Double twoStarPercentage;
    private Double threeStarPercentage;
    private Double fourStarPercentage;
    private Double fiveStarPercentage;
}