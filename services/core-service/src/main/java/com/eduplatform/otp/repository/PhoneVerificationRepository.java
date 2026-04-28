// FILE 5: PhoneVerificationRepository.java
package com.eduplatform.otp.repository;

import com.eduplatform.otp.model.PhoneVerification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PhoneVerificationRepository extends MongoRepository<PhoneVerification, String> {

    Optional<PhoneVerification> findByUserIdAndTenantId(String userId, String tenantId);

    Optional<PhoneVerification> findByPhoneNumberAndTenantId(String phoneNumber, String tenantId);
}