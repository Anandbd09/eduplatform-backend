package com.eduplatform.admin.repository;

import com.eduplatform.admin.model.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends MongoRepository<Admin, String> {
    Optional<Admin> findByUserId(String userId);

    List<Admin> findByLevel(String level);

    List<Admin> findByStatus(String status);

    List<Admin> findByLevelAndStatus(String level, String status);
}