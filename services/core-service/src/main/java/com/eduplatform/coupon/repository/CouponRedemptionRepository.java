// FILE 8: CouponRedemptionRepository.java
package com.eduplatform.coupon.repository;

import com.eduplatform.coupon.model.CouponRedemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRedemptionRepository extends MongoRepository<CouponRedemption, String> {

    Optional<CouponRedemption> findByIdAndTenantId(String id, String tenantId);

    Page<CouponRedemption> findByUserIdAndTenantId(String userId, String tenantId, Pageable pageable);

    Page<CouponRedemption> findByCouponIdAndTenantId(String couponId, String tenantId, Pageable pageable);

    Long countByUserIdAndCouponIdAndTenantId(String userId, String couponId, String tenantId);

    List<CouponRedemption> findByUserIdAndCouponIdAndStatusAndTenantId(
            String userId, String couponId, String status, String tenantId);

    @Query("{ 'status': 'SUCCESS', 'redeemedAt': { $gte: ?0 }, 'tenantId': ?1 }")
    List<CouponRedemption> findRecentSuccessfulRedemptions(LocalDateTime date, String tenantId);

    Long countByStatusAndTenantId(String status, String tenantId);
}