// FILE 8: ReferralRewardRepository.java
package com.eduplatform.referral.repository;

import com.eduplatform.referral.model.ReferralReward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReferralRewardRepository extends MongoRepository<ReferralReward, String> {

    Page<ReferralReward> findByInstructorIdAndTenantId(String instructorId, String tenantId, Pageable pageable);

    Page<ReferralReward> findByStatusAndTenantId(String status, String tenantId, Pageable pageable);

    Page<ReferralReward> findByInstructorIdAndStatusAndTenantId(String instructorId, String status, String tenantId, Pageable pageable);

    List<ReferralReward> findByStatusAndExpiresAtBeforeAndTenantId(String status, LocalDateTime date, String tenantId);

    Double findByInstructorIdAndStatusAndTenantId(String instructorId, String status, String tenantId);

    Long countByInstructorIdAndStatusAndTenantId(String instructorId, String status, String tenantId);
}