// FILE 9: CourseSimilarityRepository.java
package com.eduplatform.recommendation.repository;

import com.eduplatform.recommendation.model.CourseSimilarity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseSimilarityRepository extends MongoRepository<CourseSimilarity, String> {

    List<CourseSimilarity> findBySourceCourseIdAndTenantId(String sourceCourseId, String tenantId);

    List<CourseSimilarity> findBySourceCourseIdAndSimilarityScoreGreaterThanAndTenantId(
            String sourceCourseId, Double minScore, String tenantId);

    Optional<CourseSimilarity> findBySourceCourseIdAndTargetCourseIdAndTenantId(
            String sourceCourseId, String targetCourseId, String tenantId);
}