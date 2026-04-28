package com.eduplatform.review.service;

import com.eduplatform.review.model.Rating;
import com.eduplatform.review.dto.RatingStats;
import com.eduplatform.review.repository.RatingRepository;
import com.eduplatform.review.exception.ReviewException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    /**
     * Get rating statistics for a course
     */
    public RatingStats getRatingStats(String courseId, String tenantId) {

        if (courseId == null || courseId.isEmpty()) {
            throw new ReviewException("Course ID is required");
        }

        try {
            Rating rating = ratingRepository.findByCourseIdAndTenantId(courseId, tenantId);

            if (rating == null) {
                // Return empty stats if no ratings
                return RatingStats.builder()
                        .averageRating(0.0)
                        .totalRatings(0)
                        .oneStarCount(0)
                        .twoStarCount(0)
                        .threeStarCount(0)
                        .fourStarCount(0)
                        .fiveStarCount(0)
                        .oneStarPercentage(0.0)
                        .twoStarPercentage(0.0)
                        .threeStarPercentage(0.0)
                        .fourStarPercentage(0.0)
                        .fiveStarPercentage(0.0)
                        .build();
            }

            return RatingStats.builder()
                    .averageRating(rating.getAverageRating())
                    .totalRatings(rating.getTotalRatings())
                    .oneStarCount(rating.getOneStarCount())
                    .twoStarCount(rating.getTwoStarCount())
                    .threeStarCount(rating.getThreeStarCount())
                    .fourStarCount(rating.getFourStarCount())
                    .fiveStarCount(rating.getFiveStarCount())
                    .oneStarPercentage(rating.getPercentage(1))
                    .twoStarPercentage(rating.getPercentage(2))
                    .threeStarPercentage(rating.getPercentage(3))
                    .fourStarPercentage(rating.getPercentage(4))
                    .fiveStarPercentage(rating.getPercentage(5))
                    .build();

        } catch (Exception e) {
            log.error("Error getting rating stats for course {}", courseId, e);
            throw new ReviewException("Failed to get rating stats");
        }
    }

    /**
     * Update rating when a new review is added
     */
    public void updateRating(String courseId, Integer newRating, String tenantId) {

        if (courseId == null || courseId.isEmpty() || newRating == null) {
            return;
        }

        if (newRating < 1 || newRating > 5) {
            return;
        }

        try {
            Rating rating = ratingRepository.findByCourseIdAndTenantId(courseId, tenantId);

            if (rating == null) {
                // Create new rating
                rating = Rating.builder()
                        .id(UUID.randomUUID().toString())
                        .courseId(courseId)
                        .tenantId(tenantId)
                        .build();
            }

            updateStarCounts(rating, newRating, 1);
            recalculateAverageRating(rating);
            rating.setLastUpdated(LocalDateTime.now());
            rating.setVersion(rating.getVersion() + 1);

            ratingRepository.save(rating);
            log.info("Rating updated for course: {} with rating: {}", courseId, newRating);

        } catch (Exception e) {
            log.error("Error updating rating for course {}", courseId, e);
        }
    }

    /**
     * Update rating after a review is edited
     */
    public void updateRatingAfterEdit(String courseId, Integer oldRating, Integer newRating, String tenantId) {

        try {
            Rating rating = ratingRepository.findByCourseIdAndTenantId(courseId, tenantId);

            if (rating == null) {
                return;
            }

            // Remove old rating
            updateStarCounts(rating, oldRating, -1);
            // Add new rating
            updateStarCounts(rating, newRating, 1);

            recalculateAverageRating(rating);
            rating.setLastUpdated(LocalDateTime.now());
            rating.setVersion(rating.getVersion() + 1);

            ratingRepository.save(rating);

        } catch (Exception e) {
            log.error("Error updating rating after edit", e);
        }
    }

    /**
     * Decrement rating when a review is deleted
     */
    public void decrementRating(String courseId, Integer rating, String tenantId) {

        if (courseId == null || courseId.isEmpty() || rating == null) {
            return;
        }

        try {
            Rating ratingObj = ratingRepository.findByCourseIdAndTenantId(courseId, tenantId);

            if (ratingObj != null) {
                updateStarCounts(ratingObj, rating, -1);
                recalculateAverageRating(ratingObj);
                ratingObj.setLastUpdated(LocalDateTime.now());
                ratingObj.setVersion(ratingObj.getVersion() + 1);

                ratingRepository.save(ratingObj);
            }

        } catch (Exception e) {
            log.error("Error decrementing rating for course {}", courseId, e);
        }
    }

    /**
     * Update star counts
     */
    private void updateStarCounts(Rating rating, Integer starCount, int increment) {
        switch (starCount) {
            case 1:
                rating.setOneStarCount(Math.max(0, rating.getOneStarCount() + increment));
                break;
            case 2:
                rating.setTwoStarCount(Math.max(0, rating.getTwoStarCount() + increment));
                break;
            case 3:
                rating.setThreeStarCount(Math.max(0, rating.getThreeStarCount() + increment));
                break;
            case 4:
                rating.setFourStarCount(Math.max(0, rating.getFourStarCount() + increment));
                break;
            case 5:
                rating.setFiveStarCount(Math.max(0, rating.getFiveStarCount() + increment));
                break;
        }
    }

    /**
     * Recalculate average rating
     */
    private void recalculateAverageRating(Rating rating) {
        double sum = (rating.getOneStarCount() * 1.0) +
                (rating.getTwoStarCount() * 2.0) +
                (rating.getThreeStarCount() * 3.0) +
                (rating.getFourStarCount() * 4.0) +
                (rating.getFiveStarCount() * 5.0);

        int total = rating.getOneStarCount() + rating.getTwoStarCount() +
                rating.getThreeStarCount() + rating.getFourStarCount() +
                rating.getFiveStarCount();

        if (total > 0) {
            rating.setAverageRating(Math.round((sum / total) * 10.0) / 10.0);
            rating.setTotalRatings(total);
        } else {
            rating.setAverageRating(0.0);
            rating.setTotalRatings(0);
        }
    }
}