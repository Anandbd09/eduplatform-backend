// FILE 7: CouponCodeRepository.java
package com.eduplatform.coupon.repository;

import com.eduplatform.coupon.model.CouponCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponCodeRepository extends MongoRepository<CouponCode, String> {

    Optional<CouponCode> findByCodeAndTenantId(String code, String tenantId);

    List<CouponCode> findByCouponIdAndTenantId(String couponId, String tenantId);

    List<CouponCode> findByStatusAndTenantId(String status, String tenantId);

    Page<CouponCode> findByRedeemedByAndTenantId(String userId, String tenantId, Pageable pageable);

    Long countByStatusAndTenantId(String status, String tenantId);
}