// FILE 9: CertificateRevocationRepository.java
package com.eduplatform.certificate.repository;

import com.eduplatform.certificate.model.CertificateRevocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRevocationRepository extends MongoRepository<CertificateRevocation, String> {

    Optional<CertificateRevocation> findByCertificateIdAndTenantId(String certificateId, String tenantId);

    Optional<CertificateRevocation> findByCertificateNumberAndTenantId(String certificateNumber, String tenantId);

    Page<CertificateRevocation> findByStatusAndTenantId(String status, String tenantId, Pageable pageable);

    List<CertificateRevocation> findByReasonAndTenantId(String reason, String tenantId);
}