// FILE 5: DailyStreakRepository.java
package com.eduplatform.gamification.repository;

import com.eduplatform.gamification.model.DailyStreak;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DailyStreakRepository extends MongoRepository<DailyStreak, String> {

    Optional<DailyStreak> findByUserIdAndCourseIdAndTenantId(String userId, String courseId, String tenantId);

    Page<DailyStreak> findByUserIdAndTenantId(String userId, String tenantId, Pageable pageable);

    Page<DailyStreak> findByIsActiveTrueAndTenantId(String tenantId, Pageable pageable);
}