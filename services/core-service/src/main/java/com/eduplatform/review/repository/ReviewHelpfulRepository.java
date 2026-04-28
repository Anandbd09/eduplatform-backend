package com.eduplatform.review.repository;

import com.eduplatform.review.model.ReviewHelpful;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ReviewHelpfulRepository extends MongoRepository<ReviewHelpful, String> {

    /**
     * Find helpful marking by review and user
     */
    ReviewHelpful findByReviewIdAndUserId(String reviewId, String userId);

    /**
     * Check if user marked review as helpful
     */
    Boolean existsByReviewIdAndUserIdAndIsHelpfulTrue(String reviewId, String userId);

    /**
     * Check if user marked review as not helpful
     */
    Boolean existsByReviewIdAndUserIdAndIsHelpfulFalse(String reviewId, String userId);
}