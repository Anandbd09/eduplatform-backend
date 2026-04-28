// FILE 8: CertificateVerificationRepository.java
package com.eduplatform.certificate.repository;

import com.eduplatform.certificate.model.CertificateVerification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CertificateVerificationRepository extends MongoRepository<CertificateVerification, String> {

    Optional<CertificateVerification> findByCertificateNumberAndTenantId(String certificateNumber, String tenantId);

    Optional<CertificateVerification> findByCertificateIdAndTenantId(String certificateId, String tenantId);

    Page<CertificateVerification> findByVerificationStatusAndTenantId(String status, String tenantId, Pageable pageable);
}