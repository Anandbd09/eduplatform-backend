// FILE 6: UserPreferenceRepository.java
package com.eduplatform.recommendation.repository;

import com.eduplatform.recommendation.model.UserPreference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserPreferenceRepository extends MongoRepository<UserPreference, String> {

    Optional<UserPreference> findByUserIdAndCourseIdAndTenantId(String userId, String courseId, String tenantId);

    List<UserPreference> findByUserIdAndTenantId(String userId, String tenantId);

    List<UserPreference> findByUserIdAndInteractionDateGreaterThanAndTenantId(
            String userId, LocalDateTime date, String tenantId);

    @Query("{ 'userId': ?0, 'engagementScore': { $gte: ?1 }, 'tenantId': ?2 }")
    List<UserPreference> findHighEngagementPreferences(String userId, Double minScore, String tenantId);

    Long countByUserIdAndInteractionTypeAndTenantId(String userId, String type, String tenantId);
}