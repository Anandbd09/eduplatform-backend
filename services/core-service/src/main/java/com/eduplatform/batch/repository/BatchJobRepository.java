// FILE 7: BatchJobRepository.java
package com.eduplatform.batch.repository;

import com.eduplatform.batch.model.BatchJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BatchJobRepository extends MongoRepository<BatchJob, String> {

    Optional<BatchJob> findByJobIdAndTenantId(String jobId, String tenantId);

    Page<BatchJob> findByUserIdAndTenantId(String userId, String tenantId, Pageable pageable);

    Page<BatchJob> findByJobTypeAndTenantId(String jobType, String tenantId, Pageable pageable);

    Page<BatchJob> findByStatusAndTenantId(String status, String tenantId, Pageable pageable);

    List<BatchJob> findByStatusAndTenantId(String status, String tenantId);

    @Query("{ 'status': { $in: ['QUEUED', 'PROCESSING'] }, 'tenantId': ?0 }")
    List<BatchJob> findRunningJobs(String tenantId);

    Long countByStatusAndTenantId(String status, String tenantId);
}