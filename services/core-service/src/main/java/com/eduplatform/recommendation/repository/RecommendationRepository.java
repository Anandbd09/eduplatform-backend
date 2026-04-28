// FILE 8: RecommendationRepository.java
package com.eduplatform.recommendation.repository;

import com.eduplatform.recommendation.model.RecommendationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecommendationRepository extends MongoRepository<RecommendationRecord, String> {

    Page<RecommendationRecord> findByUserIdAndTenantId(String userId, String tenantId, Pageable pageable);

    Page<RecommendationRecord> findByUserIdAndReasonAndTenantId(String userId, String reason, String tenantId, Pageable pageable);

    Page<RecommendationRecord> findByUserIdAndClickedTrueAndTenantId(String userId, String tenantId, Pageable pageable);

    @Query("{ 'userId': ?0, 'tenantId': ?1 }")
    List<RecommendationRecord> findTopRecommendations(String userId, String tenantId, Pageable pageable);
}