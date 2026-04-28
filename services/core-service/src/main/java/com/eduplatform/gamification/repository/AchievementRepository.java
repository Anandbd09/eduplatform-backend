// FILE 7: AchievementRepository.java
package com.eduplatform.gamification.repository;

import com.eduplatform.gamification.model.Achievement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AchievementRepository extends MongoRepository<Achievement, String> {

    Optional<Achievement> findByAchievementCodeAndTenantId(String code, String tenantId);

    Page<Achievement> findByTenantId(String tenantId, Pageable pageable);
}