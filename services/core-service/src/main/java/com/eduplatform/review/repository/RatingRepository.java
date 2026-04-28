package com.eduplatform.review.repository;

import com.eduplatform.review.model.Rating;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RatingRepository extends MongoRepository<Rating, String> {

    /**
     * Find rating by course ID
     */
    Rating findByCourseId(String courseId);

    /**
     * Find rating by course ID and tenant
     */
    Rating findByCourseIdAndTenantId(String courseId, String tenantId);

    /**
     * Check if rating exists for course
     */
    Boolean existsByCourseId(String courseId);
}