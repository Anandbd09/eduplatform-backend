// FILE 9: ReferralPayoutRepository.java
package com.eduplatform.referral.repository;

import com.eduplatform.referral.model.ReferralPayout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReferralPayoutRepository extends MongoRepository<ReferralPayout, String> {

    Optional<ReferralPayout> findLatestByInstructorIdAndTenantId(String instructorId, String tenantId);

    Page<ReferralPayout> findByInstructorIdAndTenantId(String instructorId, String tenantId, Pageable pageable);

    Page<ReferralPayout> findByStatusAndTenantId(String status, String tenantId, Pageable pageable);

    List<ReferralPayout> findByStatusAndTenantId(String status, String tenantId);

    Long countByInstructorIdAndStatusAndTenantId(String instructorId, String status, String tenantId);
}