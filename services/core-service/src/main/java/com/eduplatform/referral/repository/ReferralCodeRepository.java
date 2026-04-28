// FILE 6: ReferralCodeRepository.java
package com.eduplatform.referral.repository;

import com.eduplatform.referral.model.ReferralCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReferralCodeRepository extends MongoRepository<ReferralCode, String> {

    Optional<ReferralCode> findByReferralCodeAndTenantId(String referralCode, String tenantId);

    Page<ReferralCode> findByInstructorIdAndTenantId(String instructorId, String tenantId, Pageable pageable);

    Page<ReferralCode> findByStatusAndTenantId(String status, String tenantId, Pageable pageable);

    List<ReferralCode> findByInstructorIdAndStatusAndTenantId(String instructorId, String status, String tenantId);

    @Query("{ 'status': 'ACTIVE', 'expiresAt': { $gt: new Date() }, 'tenantId': ?0 }")
    List<ReferralCode> findActiveCodes(String tenantId);

    Long countByInstructorIdAndTenantId(String instructorId, String tenantId);
}