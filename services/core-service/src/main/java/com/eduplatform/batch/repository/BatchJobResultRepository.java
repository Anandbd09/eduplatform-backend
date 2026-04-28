// FILE 8: BatchJobResultRepository.java
package com.eduplatform.batch.repository;

import com.eduplatform.batch.model.BatchJobResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BatchJobResultRepository extends MongoRepository<BatchJobResult, String> {

    Page<BatchJobResult> findByJobIdAndTenantId(String jobId, String tenantId, Pageable pageable);

    Page<BatchJobResult> findByJobIdAndStatusAndTenantId(String jobId, String status, String tenantId, Pageable pageable);

    List<BatchJobResult> findByJobIdAndStatusAndTenantId(String jobId, String status, String tenantId);

    Long countByJobIdAndStatusAndTenantId(String jobId, String status, String tenantId);
}