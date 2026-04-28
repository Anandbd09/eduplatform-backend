// FILE 10: CouponAnalyticsRepository.java
package com.eduplatform.coupon.repository;

import com.eduplatform.coupon.model.CouponAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CouponAnalyticsRepository extends MongoRepository<CouponAnalytics, String> {

    Optional<CouponAnalytics> findByCouponIdAndTenantId(String couponId, String tenantId);
}