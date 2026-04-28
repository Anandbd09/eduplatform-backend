// FILE 7: CourseVectorRepository.java
package com.eduplatform.recommendation.repository;

import com.eduplatform.recommendation.model.CourseVector;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseVectorRepository extends MongoRepository<CourseVector, String> {

    Optional<CourseVector> findByCourseIdAndTenantId(String courseId, String tenantId);

    List<CourseVector> findByCategoryAndTenantId(String category, String tenantId);

    List<CourseVector> findByLevelAndTenantId(String level, String tenantId);

    List<CourseVector> findByRatingGreaterThanAndTenantId(Double minRating, String tenantId);
}