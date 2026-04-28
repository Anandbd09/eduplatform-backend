// FILE 7: ReferralClickRepository.java
package com.eduplatform.referral.repository;

import com.eduplatform.referral.model.ReferralClick;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReferralClickRepository extends MongoRepository<ReferralClick, String> {

    Page<ReferralClick> findByReferralCodeAndTenantId(String referralCode, String tenantId, Pageable pageable);

    Page<ReferralClick> findByInstructorIdAndTenantId(String instructorId, String tenantId, Pageable pageable);

    Page<ReferralClick> findByStatusAndTenantId(String status, String tenantId, Pageable pageable);

    List<ReferralClick> findByStatusAndClickedAtBetweenAndTenantId(String status, LocalDateTime from, LocalDateTime to, String tenantId);

    Long countByReferralCodeAndTenantId(String referralCode, String tenantId);

    Long countByInstructorIdAndStatusAndTenantId(String instructorId, String status, String tenantId);
}