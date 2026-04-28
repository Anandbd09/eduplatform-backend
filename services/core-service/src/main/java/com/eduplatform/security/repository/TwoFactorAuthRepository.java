// FILE 6: TwoFactorAuthRepository.java
package com.eduplatform.security.repository;

import com.eduplatform.security.model.TwoFactorAuth;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TwoFactorAuthRepository extends MongoRepository<TwoFactorAuth, String> {

    Optional<TwoFactorAuth> findByUserIdAndTenantId(String userId, String tenantId);
}