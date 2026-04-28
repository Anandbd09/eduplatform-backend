// FILE 5: ExportJobRepository.java
package com.eduplatform.export.repository;

import com.eduplatform.export.model.ExportJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExportJobRepository extends MongoRepository<ExportJob, String> {

    Optional<ExportJob> findByJobIdAndTenantId(String jobId, String tenantId);

    Page<ExportJob> findByUserIdAndTenantId(String userId, String tenantId, Pageable pageable);

    Page<ExportJob> findByStatusAndTenantId(String status, String tenantId, Pageable pageable);

    Page<ExportJob> findByExportTypeAndTenantId(String exportType, String tenantId, Pageable pageable);

    List<ExportJob> findByExpiresAtBeforeAndTenantId(LocalDateTime before, String tenantId);
}