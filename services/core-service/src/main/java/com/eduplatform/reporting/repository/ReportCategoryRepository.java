package com.eduplatform.reporting.repository;

import com.eduplatform.reporting.model.ReportCategory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportCategoryRepository extends MongoRepository<ReportCategory, String> {

    /**
     * Find by code
     */
    Optional<ReportCategory> findByCodeAndTenantId(String code, String tenantId);

    /**
     * Find all categories for tenant
     */
    List<ReportCategory> findByTenantId(String tenantId);
}