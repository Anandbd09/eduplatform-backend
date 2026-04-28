// FILE 7: LoginAttemptRepository.java
package com.eduplatform.security.repository;

import com.eduplatform.security.model.LoginAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends MongoRepository<LoginAttempt, String> {

    Page<LoginAttempt> findByUserIdAndTenantId(String userId, String tenantId, Pageable pageable);

    List<LoginAttempt> findByEmailAndStatusAndAttemptedAtAfterAndTenantId(
            String email, String status, LocalDateTime after, String tenantId);

    Long countByUserIdAndStatusAndAttemptedAtAfterAndTenantId(
            String userId, String status, LocalDateTime after, String tenantId);
}