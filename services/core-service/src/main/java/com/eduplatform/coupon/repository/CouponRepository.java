// FILE 6: CouponRepository.java
package com.eduplatform.coupon.repository;

import com.eduplatform.coupon.model.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends MongoRepository<Coupon, String> {

    Optional<Coupon> findByCodeAndTenantId(String code, String tenantId);

    Page<Coupon> findByStatusAndTenantId(String status, String tenantId, Pageable pageable);

    List<Coupon> findByStatusAndValidFromBeforeAndValidUntilAfterAndTenantId(
            String status, LocalDateTime from, LocalDateTime until, String tenantId);

    Page<Coupon> findByDiscountTypeAndTenantId(String discountType, String tenantId, Pageable pageable);

    @Query("{ 'status': 'ACTIVE', 'validFrom': { $lte: new Date() }, 'validUntil': { $gte: new Date() }, 'tenantId': ?0 }")
    List<Coupon> findActiveCoupons(String tenantId);

    Long countByStatusAndTenantId(String status, String tenantId);
}