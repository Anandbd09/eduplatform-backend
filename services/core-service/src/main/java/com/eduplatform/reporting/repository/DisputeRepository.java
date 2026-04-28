package com.eduplatform.reporting.repository;

import com.eduplatform.reporting.model.Dispute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DisputeRepository extends MongoRepository<Dispute, String> {

    /**
     * Find by report ID
     */
    Optional<Dispute> findByReportIdAndTenantId(String reportId, String tenantId);

    /**
     * Find all disputes in queue (not assigned/resolved)
     */
    @Query("{ 'status': { $in: ['QUEUED', 'ASSIGNED'] }, 'tenantId': ?0 }")
    Page<Dispute> findQueuedDisputes(String tenantId, Pageable pageable);

    /**
     * Find disputes assigned to admin
     */
    Page<Dispute> findByAssignedToAndTenantId(String adminId, String tenantId, Pageable pageable);

    /**
     * Find disputes by status
     */
    Page<Dispute> findByStatusAndTenantId(String status, String tenantId, Pageable pageable);

    /**
     * Find overdue disputes (deadline passed, not resolved)
     */
    @Query("{ 'responseDeadline': { $lt: new Date() }, 'status': { $ne: 'RESOLVED' }, 'tenantId': ?0 }")
    List<Dispute> findOverdueDisputes(String tenantId);

    /**
     * Find disputes for disputed user
     */
    Page<Dispute> findByDisputedUserIdAndTenantId(String userId, String tenantId, Pageable pageable);

    /**
     * Sort by priority for queue ordering
     */
    @Query("{ 'status': 'QUEUED', 'tenantId': ?0 }")
    List<Dispute> findQueuedOrderByPriority(String tenantId);

    /**
     * Count unresolved
     */
    Long countByStatusAndTenantId(String status, String tenantId);
}