// FILE 10: ReferralAnalyticsRepository.java
package com.eduplatform.referral.repository;

import com.eduplatform.referral.model.ReferralAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ReferralAnalyticsRepository extends MongoRepository<ReferralAnalytics, String> {

    Optional<ReferralAnalytics> findByReferralCodeIdAndTenantId(String referralCodeId, String tenantId);
}