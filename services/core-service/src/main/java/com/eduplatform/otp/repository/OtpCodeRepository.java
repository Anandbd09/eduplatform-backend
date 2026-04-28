// FILE 4: OtpCodeRepository.java
package com.eduplatform.otp.repository;

import com.eduplatform.otp.model.OtpCode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OtpCodeRepository extends MongoRepository<OtpCode, String> {

    Optional<OtpCode> findByUserIdAndContactTargetAndStatusAndTenantId(String userId, String contactTarget, String status, String tenantId);

    Optional<OtpCode> findByUserIdAndPurposeAndStatusAndTenantId(String userId, String purpose, String status, String tenantId);
}