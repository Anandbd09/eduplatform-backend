// FILE 9: UserImportRepository.java
package com.eduplatform.batch.repository;

import com.eduplatform.batch.model.UserImport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserImportRepository extends MongoRepository<UserImport, String> {

    Optional<UserImport> findByJobIdAndTenantId(String jobId, String tenantId);
}