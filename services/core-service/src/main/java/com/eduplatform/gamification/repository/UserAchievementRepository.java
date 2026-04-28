// FILE 8: UserAchievementRepository.java
package com.eduplatform.gamification.repository;

import com.eduplatform.gamification.model.UserAchievement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAchievementRepository extends MongoRepository<UserAchievement, String> {

    Page<UserAchievement> findByUserIdAndIsUnlockedTrueAndTenantId(String userId, Boolean isUnlocked, String tenantId, Pageable pageable);

    Long countByUserIdAndIsUnlockedTrueAndTenantId(String userId, Boolean isUnlocked, String tenantId);
}