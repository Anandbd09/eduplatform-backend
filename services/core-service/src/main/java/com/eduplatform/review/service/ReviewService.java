package com.eduplatform.review.service;

import com.eduplatform.review.model.Review;
import com.eduplatform.review.model.ReviewHelpful;
import com.eduplatform.review.dto.ReviewRequest;
import com.eduplatform.review.dto.ReviewResponse;
import com.eduplatform.review.repository.ReviewRepository;
import com.eduplatform.review.repository.ReviewHelpfulRepository;
import com.eduplatform.review.exception.ReviewException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewHelpfulRepository reviewHelpfulRepository;

    @Autowired
    private RatingService ratingService;

    /**
     * Create a new review for a course
     */
    public ReviewResponse createReview(String courseId, ReviewRequest request, String userId,
                                       String userName, String userEmail, String tenantId) {

        // Validation
        if (courseId == null || courseId.isEmpty()) {
            throw new ReviewException("Course ID is required");
        }
        if (request == null) {
            throw new ReviewException("Review request is required");
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new ReviewException("Rating must be between 1 and 5");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new ReviewException("Review title is required");
        }
        if (request.getTitle().length() > 100) {
            throw new ReviewException("Title cannot exceed 100 characters");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new ReviewException("Review content is required");
        }
        if (request.getContent().length() > 5000) {
            throw new ReviewException("Content cannot exceed 5000 characters");
        }

        // Check if user already reviewed this course
        Review existing = reviewRepository.findByCourseIdAndUserId(courseId, userId);
        if (existing != null) {
            throw new ReviewException("You have already reviewed this course. Edit your existing review instead.");
        }

        try {
            // Create review
            Review review = Review.builder()
                    .id(UUID.randomUUID().toString())
                    .courseId(courseId)
                    .userId(userId)
                    .userName(userName)
                    .userEmail(userEmail)
                    .rating(request.getRating())
                    .title(request.getTitle().trim())
                    .content(request.getContent().trim())
                    .status("APPROVED") // Auto-approve for now
                    .isVerifiedPurchase(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .helpfulCount(0)
                    .unHelpfulCount(0)
                    .tags(request.getTags() != null ? request.getTags() : new ArrayList<>())
                    .version(0L)
                    .build();

            Review saved = reviewRepository.save(review);

            // Update rating statistics
            try {
                ratingService.updateRating(courseId, request.getRating(), tenantId);
            } catch (Exception e) {
                log.error("Error updating rating for course {}", courseId, e);
                // Continue - don't fail the review creation
            }

            log.info("Review created successfully: {} for course: {} by user: {}",
                    saved.getId(), courseId, userId);
            return convertToResponse(saved);

        } catch (ReviewException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating review", e);
            throw new ReviewException("Failed to create review: " + e.getMessage());
        }
    }

    /**
     * Update an existing review
     */
    public ReviewResponse updateReview(String reviewId, ReviewRequest request,
                                       String userId, String tenantId) {

        if (reviewId == null || reviewId.isEmpty()) {
            throw new ReviewException("Review ID is required");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException("Review not found"));

        // Check if user owns this review
        if (!review.getUserId().equals(userId)) {
            throw new ReviewException("You can only edit your own reviews");
        }

        // Check if review can still be edited (30 days)
        if (!review.canEdit(LocalDateTime.now())) {
            throw new ReviewException("Reviews can only be edited within 30 days of creation");
        }

        try {
            // Validate new rating if provided
            if (request.getRating() != null) {
                if (request.getRating() < 1 || request.getRating() > 5) {
                    throw new ReviewException("Rating must be between 1 and 5");
                }
            }

            Integer oldRating = review.getRating();

            // Update fields
            if (request.getRating() != null) review.setRating(request.getRating());
            if (request.getTitle() != null && !request.getTitle().isEmpty()) {
                review.setTitle(request.getTitle().trim());
            }
            if (request.getContent() != null && !request.getContent().isEmpty()) {
                review.setContent(request.getContent().trim());
            }

            review.setUpdatedAt(LocalDateTime.now());
            review.setVersion(review.getVersion() + 1);

            Review updated = reviewRepository.save(review);

            // Update rating if changed
            if (!oldRating.equals(request.getRating())) {
                try {
                    ratingService.updateRatingAfterEdit(review.getCourseId(), oldRating, request.getRating(), tenantId);
                } catch (Exception e) {
                    log.error("Error updating rating", e);
                }
            }

            log.info("Review updated: {}", reviewId);
            return convertToResponse(updated);

        } catch (ReviewException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating review", e);
            throw new ReviewException("Failed to update review");
        }
    }

    /**
     * Delete a review
     */
    public void deleteReview(String reviewId, String userId, String tenantId) {

        if (reviewId == null || reviewId.isEmpty()) {
            throw new ReviewException("Review ID is required");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException("Review not found"));

        // Check if user owns this review
        if (!review.getUserId().equals(userId)) {
            throw new ReviewException("You can only delete your own reviews");
        }

        try {
            Integer rating = review.getRating();
            reviewRepository.deleteById(reviewId);

            // Update rating statistics
            try {
                ratingService.decrementRating(review.getCourseId(), rating, tenantId);
            } catch (Exception e) {
                log.error("Error updating rating", e);
            }

            log.info("Review deleted: {}", reviewId);

        } catch (Exception e) {
            log.error("Error deleting review", e);
            throw new ReviewException("Failed to delete review");
        }
    }

    /**
     * Get all approved reviews for a course
     */
    public Page<ReviewResponse> getCourseReviews(String courseId, int page, int size, String tenantId) {

        if (courseId == null || courseId.isEmpty()) {
            throw new ReviewException("Course ID is required");
        }

        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100); // Max 100 per page

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            Page<Review> reviews = reviewRepository.findByCourseIdAndStatusAndTenantId(
                    courseId, "APPROVED", tenantId, pageable
            );

            return reviews.map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error fetching course reviews", e);
            throw new ReviewException("Failed to fetch reviews: " + e.getMessage());
        }
    }

    /**
     * Get review by ID
     */
    public ReviewResponse getReview(String reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException("Review not found"));

        return convertToResponse(review);
    }

    /**
     * Mark review as helpful or not helpful
     */
    public void markHelpful(String reviewId, String userId, boolean helpful) {

        if (reviewId == null || reviewId.isEmpty()) {
            throw new ReviewException("Review ID is required");
        }
        if (userId == null || userId.isEmpty()) {
            throw new ReviewException("User ID is required");
        }

        try {
            Review review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new ReviewException("Review not found"));

            // Prevent self-voting
            if (review.getUserId().equals(userId)) {
                throw new ReviewException("You cannot vote on your own review");
            }

            ReviewHelpful existing = reviewHelpfulRepository.findByReviewIdAndUserId(reviewId, userId);

            if (existing != null) {
                // User already voted
                if (existing.getIsHelpful() == helpful) {
                    // Same vote - remove it
                    reviewHelpfulRepository.delete(existing);

                    if (helpful) {
                        review.setHelpfulCount(Math.max(0, review.getHelpfulCount() - 1));
                    } else {
                        review.setUnHelpfulCount(Math.max(0, review.getUnHelpfulCount() - 1));
                    }
                } else {
                    // Different vote - switch it
                    existing.setIsHelpful(helpful);
                    existing.setMarkedAt(LocalDateTime.now());
                    reviewHelpfulRepository.save(existing);

                    if (helpful) {
                        review.setHelpfulCount(review.getHelpfulCount() + 1);
                        review.setUnHelpfulCount(Math.max(0, review.getUnHelpfulCount() - 1));
                    } else {
                        review.setUnHelpfulCount(review.getUnHelpfulCount() + 1);
                        review.setHelpfulCount(Math.max(0, review.getHelpfulCount() - 1));
                    }
                }
            } else {
                // New vote
                ReviewHelpful reviewHelpful = ReviewHelpful.builder()
                        .id(UUID.randomUUID().toString())
                        .reviewId(reviewId)
                        .userId(userId)
                        .isHelpful(helpful)
                        .markedAt(LocalDateTime.now())
                        .build();

                reviewHelpfulRepository.save(reviewHelpful);

                if (helpful) {
                    review.setHelpfulCount(review.getHelpfulCount() + 1);
                } else {
                    review.setUnHelpfulCount(review.getUnHelpfulCount() + 1);
                }
            }

            review.setUpdatedAt(LocalDateTime.now());
            reviewRepository.save(review);

            log.info("Review marked as helpful={}: {} by user: {}", helpful, reviewId, userId);

        } catch (ReviewException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error marking review", e);
            throw new ReviewException("Failed to mark review: " + e.getMessage());
        }
    }

    /**
     * Get most helpful reviews for a course
     */
    public Page<ReviewResponse> getMostHelpfulReviews(String courseId, int page, int size, String tenantId) {

        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);

            Pageable pageable = PageRequest.of(page, size,
                    Sort.by("helpfulCount").descending()
                            .and(Sort.by("createdAt").descending())
            );

            Page<Review> reviews = reviewRepository.findByCourseIdAndStatusAndTenantId(
                    courseId, "APPROVED", tenantId, pageable
            );

            return reviews.map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error fetching most helpful reviews", e);
            throw new ReviewException("Failed to fetch reviews");
        }
    }

    /**
     * Convert Review to ReviewResponse
     */
    private ReviewResponse convertToResponse(Review review) {
        if (review == null) {
            return null;
        }

        return ReviewResponse.builder()
                .id(review.getId())
                .courseId(review.getCourseId())
                .userId(review.getUserId())
                .userName(review.getUserName())
                .userEmail(review.getUserEmail())
                .userAvatar(review.getUserAvatar())
                .rating(review.getRating())
                .title(review.getTitle())
                .content(review.getContent())
                .helpfulCount(review.getHelpfulCount())
                .unHelpfulCount(review.getUnHelpfulCount())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .status(review.getStatus())
                .isVerifiedPurchase(review.getIsVerifiedPurchase())
                .tags(review.getTags())
                .ageInDays(review.getAgeInDays())
                .canEdit(review.canEdit(LocalDateTime.now()))
                .build();
    }
}