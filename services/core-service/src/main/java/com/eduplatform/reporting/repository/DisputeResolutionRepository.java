package com.eduplatform.reporting.repository;

import com.eduplatform.reporting.model.DisputeResolution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DisputeResolutionRepository extends MongoRepository<DisputeResolution, String> {

    /**
     * Find resolution by dispute
     */
    Optional<DisputeResolution> findByDisputeIdAndTenantId(String disputeId, String tenantId);

    /**
     * Find resolutions by decision
     */
    Page<DisputeResolution> findByDecisionAndTenantId(String decision, String tenantId, Pageable pageable);

    /**
     * Find appealed resolutions
     */
    Page<DisputeResolution> findByAppealedTrueAndTenantId(String tenantId, Pageable pageable);

    /**
     * Find resolutions by action type
     */
    Page<DisputeResolution> findByActionTypeAndTenantId(String actionType, String tenantId, Pageable pageable);

    /**
     * Find recent resolutions
     */
    List<DisputeResolution> findByResolvedAtAfterAndTenantId(LocalDateTime date, String tenantId);
}