// FILE 6: UserPointsRepository.java
package com.eduplatform.gamification.repository;

import com.eduplatform.gamification.model.UserPoints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserPointsRepository extends MongoRepository<UserPoints, String> {

    Optional<UserPoints> findByUserIdAndTenantId(String userId, String tenantId);

    Page<UserPoints> findByTenantIdOrderByTotalPointsDesc(String tenantId, Pageable pageable);

    Long countByCurrentLevelGreaterThanAndTenantId(Integer level, String tenantId);
}