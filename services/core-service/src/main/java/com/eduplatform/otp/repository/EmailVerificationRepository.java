// FILE 6: EmailVerificationRepository.java
package com.eduplatform.otp.repository;

import com.eduplatform.otp.model.EmailVerification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends MongoRepository<EmailVerification, String> {

    Optional<EmailVerification> findByUserIdAndTenantId(String userId, String tenantId);

    Optional<EmailVerification> findByEmailAddressAndTenantId(String emailAddress, String tenantId);
}