package com.eduplatform.review.repository;

import com.eduplatform.review.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    /**
     * Find a review by course and user (one per user per course)
     */
    Review findByCourseIdAndUserId(String courseId, String userId);

    /**
     * Find all approved reviews for a course with pagination
     */
    Page<Review> findByCourseIdAndStatusAndTenantId(String courseId, String status, String tenantId, Pageable pageable);

    /**
     * Find all reviews for a course
     */
    Page<Review> findByCourseIdAndTenantId(String courseId, String tenantId, Pageable pageable);

    /**
     * Find all reviews by a user
     */
    List<Review> findByUserIdAndTenantId(String userId, String tenantId);

    /**
     * Find all reviews awaiting approval
     */
    List<Review> findByStatusAndTenantId(String status, String tenantId);

    /**
     * Count reviews for a course
     */
    Long countByCourseIdAndStatusAndTenantId(String courseId, String status, String tenantId);

    /**
     * Find reviews within a date range
     */
    @Query("{ 'courseId': ?0, 'createdAt': { $gte: ?1, $lte: ?2 }, 'tenantId': ?3 }")
    List<Review> findReviewsInDateRange(String courseId, LocalDateTime startDate, LocalDateTime endDate, String tenantId);

    /**
     * Find reviews with minimum helpfulness
     */
    @Query("{ 'courseId': ?0, 'helpfulCount': { $gte: ?1 }, 'tenantId': ?2 }")
    List<Review> findMostHelpfulReviews(String courseId, Integer minHelpful, String tenantId);
}