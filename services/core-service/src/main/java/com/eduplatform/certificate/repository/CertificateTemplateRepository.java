// FILE 7: CertificateTemplateRepository.java
package com.eduplatform.certificate.repository;

import com.eduplatform.certificate.model.CertificateTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateTemplateRepository extends MongoRepository<CertificateTemplate, String> {

    Optional<CertificateTemplate> findByNameAndTenantId(String name, String tenantId);

    Page<CertificateTemplate> findByStatusAndTenantId(String status, String tenantId, Pageable pageable);

    List<CertificateTemplate> findByStatusAndTenantId(String status, String tenantId);
}