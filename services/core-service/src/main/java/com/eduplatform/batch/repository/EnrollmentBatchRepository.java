// FILE 11: EnrollmentBatchRepository.java
package com.eduplatform.batch.repository;

import com.eduplatform.batch.model.EnrollmentBatch;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EnrollmentBatchRepository extends MongoRepository<EnrollmentBatch, String> {

    Optional<EnrollmentBatch> findByJobIdAndTenantId(String jobId, String tenantId);
}