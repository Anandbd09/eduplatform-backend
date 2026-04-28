// FILE 10: CertificateBadgeRepository.java
package com.eduplatform.certificate.repository;

import com.eduplatform.certificate.model.CertificateBadge;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateBadgeRepository extends MongoRepository<CertificateBadge, String> {

    Optional<CertificateBadge> findByCertificateIdAndTenantId(String certificateId, String tenantId);

    List<CertificateBadge> findByIsPublicAndTenantId(Boolean isPublic, String tenantId);
}