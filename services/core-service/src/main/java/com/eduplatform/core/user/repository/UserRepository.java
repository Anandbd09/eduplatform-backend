package com.eduplatform.core.user.repository;

import com.eduplatform.core.user.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndTenantId(String id, String tenantId);

    Optional<User> findByPasswordResetTokenHash(String passwordResetTokenHash);

    boolean existsByEmail(String email);
}
