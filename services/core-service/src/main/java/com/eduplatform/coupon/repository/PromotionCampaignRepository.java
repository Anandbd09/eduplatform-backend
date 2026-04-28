// FILE 9: PromotionCampaignRepository.java
package com.eduplatform.coupon.repository;

import com.eduplatform.coupon.model.PromotionCampaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionCampaignRepository extends MongoRepository<PromotionCampaign, String> {

    Optional<PromotionCampaign> findByCampaignCodeAndTenantId(String code, String tenantId);

    Page<PromotionCampaign> findByStatusAndTenantId(String status, String tenantId, Pageable pageable);

    List<PromotionCampaign> findByStatusAndStartDateBeforeAndEndDateAfterAndTenantId(
            String status, LocalDateTime start, LocalDateTime end, String tenantId);

    Page<PromotionCampaign> findByTypeAndTenantId(String type, String tenantId, Pageable pageable);
}