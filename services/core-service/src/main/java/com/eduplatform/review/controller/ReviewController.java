package com.eduplatform.review.controller;

import com.eduplatform.review.dto.ReviewCreateRequest;
import com.eduplatform.review.service.ReviewService;
import com.eduplatform.review.service.RatingService;
import com.eduplatform.review.dto.ReviewRequest;
import com.eduplatform.review.exception.ReviewException;
import com.eduplatform.core.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private RatingService ratingService;

    /**
     * CREATE REVIEW
     * POST /api/v1/reviews/courses/{courseId}
     *
     * Headers required:
     * - X-User-Id: User ID
     * - X-User-Name: User Name
     * - X-User-Email: User Email
     * - X-Tenant-Id: Tenant ID
     *
     * Request body:
     * {
     *   "rating": 5,
     *   "title": "Excellent Course",
     *   "content": "Very helpful and well structured",
     *   "tags": ["beginner-friendly", "comprehensive"]
     * }
     */
    @PostMapping("/courses/{courseId}")
    public ResponseEntity<?> createReview(
            @PathVariable String courseId,
            @Valid @RequestBody ReviewCreateRequest request,  // Use ReviewCreateRequest instead
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Name", required = false) String userName,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            // Sanitize input
            request.sanitize();

            // Validate using annotations (automatic)
            // request.validate();  // Optional: manual validation

            // Convert to ReviewRequest
            ReviewRequest reviewRequest = ReviewRequest.builder()
                    .rating(request.getRating())
                    .title(request.getTitle())
                    .content(request.getContent())
                    .tags(request.getTags())
                    .build();

            var review = reviewService.createReview(courseId, reviewRequest, userId, userName, userEmail, tenantId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(review, "Review created successfully"));

        } catch (Exception e) {
            log.error("Error creating review", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "REVIEW_CREATE_FAILED"));
        }
    }

    /**
     * GET COURSE REVIEWS
     * GET /api/v1/reviews/courses/{courseId}?page=0&size=10&sort=newest
     *
     * Parameters:
     * - page: Page number (default: 0)
     * - size: Page size (default: 10, max: 100)
     * - sort: Sort order (newest, oldest, helpful)
     */
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<?> getCourseReviews(
            @PathVariable String courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            Page<?> reviews = reviewService.getCourseReviews(courseId, page, size, tenantId);

            return ResponseEntity.ok(ApiResponse.success(reviews, "Reviews retrieved successfully"));

        } catch (ReviewException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "REVIEW_FETCH_FAILED"));

        } catch (Exception e) {
            log.error("Error fetching reviews", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch reviews", "REVIEW_FETCH_FAILED"));
        }
    }

    /**
     * GET REVIEW BY ID
     * GET /api/v1/reviews/{reviewId}
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReview(@PathVariable String reviewId) {

        try {
            var review = reviewService.getReview(reviewId);

            return ResponseEntity.ok(ApiResponse.success(review, "Review retrieved"));

        } catch (ReviewException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "REVIEW_FETCH_FAILED"));

        } catch (Exception e) {
            log.error("Error fetching review", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch review", "REVIEW_FETCH_FAILED"));
        }
    }

    /**
     * GET RATING STATISTICS
     * GET /api/v1/reviews/courses/{courseId}/ratings
     */
    @GetMapping("/courses/{courseId}/ratings")
    public ResponseEntity<?> getRatingStats(
            @PathVariable String courseId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            var stats = ratingService.getRatingStats(courseId, tenantId);

            return ResponseEntity.ok(ApiResponse.success(stats, "Rating statistics retrieved"));

        } catch (ReviewException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "RATING_STATS_FETCH_FAILED"));

        } catch (Exception e) {
            log.error("Error fetching rating stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch statistics", "RATING_STATS_FETCH_FAILED"));
        }
    }

    /**
     * UPDATE REVIEW
     * PUT /api/v1/reviews/{reviewId}
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable String reviewId,
            @RequestBody ReviewRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            var review = reviewService.updateReview(reviewId, request, userId, tenantId);

            return ResponseEntity.ok(ApiResponse.success(review, "Review updated successfully"));

        } catch (ReviewException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "REVIEW_UPDATE_FAILED"));

        } catch (Exception e) {
            log.error("Error updating review", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update review", "REVIEW_UPDATE_FAILED"));
        }
    }

    /**
     * DELETE REVIEW
     * DELETE /api/v1/reviews/{reviewId}
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable String reviewId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            reviewService.deleteReview(reviewId, userId, tenantId);

            return ResponseEntity.ok(ApiResponse.success(null, "Review deleted successfully"));

        } catch (ReviewException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "REVIEW_DELETE_FAILED"));

        } catch (Exception e) {
            log.error("Error deleting review", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete review", "REVIEW_DELETE_FAILED"));
        }
    }

    /**
     * MARK REVIEW AS HELPFUL
     * POST /api/v1/reviews/{reviewId}/helpful?helpful=true
     */
    @PostMapping("/{reviewId}/helpful")
    public ResponseEntity<?> markHelpful(
            @PathVariable String reviewId,
            @RequestParam boolean helpful,
            @RequestHeader("X-User-Id") String userId) {

        try {
            reviewService.markHelpful(reviewId, userId, helpful);

            return ResponseEntity.ok(ApiResponse.success(
                    null,
                    helpful ? "Marked as helpful" : "Marked as not helpful"
            ));

        } catch (ReviewException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "REVIEW_MARK_HELPFUL_FAILED"));

        } catch (Exception e) {
            log.error("Error marking review", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to mark review", "REVIEW_MARK_HELPFUL_FAILED"));
        }
    }

    /**
     * GET MOST HELPFUL REVIEWS
     * GET /api/v1/reviews/courses/{courseId}/helpful?page=0&size=10
     */
    @GetMapping("/courses/{courseId}/helpful")
    public ResponseEntity<?> getMostHelpfulReviews(
            @PathVariable String courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-Tenant-Id") String tenantId) {

        try {
            Page<?> reviews = reviewService.getMostHelpfulReviews(courseId, page, size, tenantId);

            return ResponseEntity.ok(ApiResponse.success(reviews, "Most helpful reviews retrieved"));

        } catch (ReviewException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "HELPFUL_REVIEW_FETCH_FAILED"));

        } catch (Exception e) {
            log.error("Error fetching helpful reviews", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch reviews", "HELPFUL_REVIEW_FETCH_FAILED"));
        }
    }
}
