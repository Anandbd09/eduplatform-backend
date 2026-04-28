// FILE 6: CertificateRepository.java
package com.eduplatform.certificate.repository;

import com.eduplatform.certificate.model.Certificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends MongoRepository<Certificate, String> {

    Optional<Certificate> findByUserIdAndCourseIdAndTenantId(String userId, String courseId, String tenantId);

    Optional<Certificate> findByCertificateNumberAndTenantId(String certificateNumber, String tenantId);

    Page<Certificate> findByUserIdAndTenantId(String userId, String tenantId, Pageable pageable);

    Page<Certificate> findByStatusAndTenantId(String status, String tenantId, Pageable pageable);

    List<Certificate> findByExpiresAtBeforeAndStatusAndTenantId(LocalDateTime date, String status, String tenantId);

    @Query("{ 'status': 'ACTIVE', 'expiresAt': { $gt: new Date() }, 'tenantId': ?0 }")
    List<Certificate> findValidCertificates(String tenantId);

    Page<Certificate> findByCourseIdAndTenantId(String courseId, String tenantId, Pageable pageable);

    Long countByStatusAndTenantId(String status, String tenantId);

    Long countByUserIdAndTenantId(String userId, String tenantId);
}