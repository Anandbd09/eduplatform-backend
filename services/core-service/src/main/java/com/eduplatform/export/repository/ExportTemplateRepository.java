// FILE 6: ExportTemplateRepository.java
package com.eduplatform.export.repository;

import com.eduplatform.export.model.ExportTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExportTemplateRepository extends MongoRepository<ExportTemplate, String> {

    Page<ExportTemplate> findByCreatedByAndTenantId(String createdBy, String tenantId, Pageable pageable);

    List<ExportTemplate> findBySourceEntityAndFormatAndTenantId(String sourceEntity, String format, String tenantId);

    Page<ExportTemplate> findByIsPublicAndTenantId(Boolean isPublic, String tenantId, Pageable pageable);
}