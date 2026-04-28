package com.eduplatform.reporting.config;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@Configuration
@EnableTransactionManagement
public class ReportingConfig {

    private final MongoTemplate mongoTemplate;

    public ReportingConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initializeIndexes() {
        try {
            log.info("Initializing Reporting & Disputes indexes...");

            IndexOperations reportIndexOps = mongoTemplate.indexOps("reports");
            ensureIndex(reportIndexOps, new Index()
                    .on("reportedEntityId", Sort.Direction.ASC)
                    .on("reportedEntityType", Sort.Direction.ASC)
                    .on("reporterId", Sort.Direction.ASC)
                    .on("tenantId", Sort.Direction.ASC)
                    .unique()
                    .named("reportedEntity_reporter_idx"), "reports");
            ensureIndex(reportIndexOps, new Index().on("status", Sort.Direction.ASC).named("status_idx"), "reports");
            ensureIndex(reportIndexOps, new Index().on("severity", Sort.Direction.DESC).named("severity_desc_idx"), "reports");
            ensureIndex(reportIndexOps, new Index().on("category", Sort.Direction.ASC).named("category_idx"), "reports");
            ensureIndex(reportIndexOps, new Index().on("createdAt", Sort.Direction.DESC).named("createdAt_desc_idx"), "reports");
            ensureIndex(reportIndexOps, new Index().on("reporterId", Sort.Direction.ASC).named("reporterId_idx"), "reports");
            log.info("Reports indexes checked");

            IndexOperations disputeIndexOps = mongoTemplate.indexOps("disputes");
            ensureIndex(disputeIndexOps, new Index()
                    .on("reportId", Sort.Direction.ASC)
                    .on("tenantId", Sort.Direction.ASC)
                    .unique()
                    .named("reportId_idx"), "disputes");
            ensureIndex(disputeIndexOps, new Index().on("status", Sort.Direction.ASC).named("status_idx"), "disputes");
            ensureIndex(disputeIndexOps, new Index().on("priority", Sort.Direction.DESC).named("priority_desc_idx"), "disputes");
            ensureIndex(disputeIndexOps, new Index().on("assignedTo", Sort.Direction.ASC).named("assignedTo_idx"), "disputes");
            ensureIndex(disputeIndexOps, new Index().on("responseDeadline", Sort.Direction.ASC).named("responseDeadline_idx"), "disputes");
            ensureIndex(disputeIndexOps, new Index().on("disputedUserId", Sort.Direction.ASC).named("disputedUserId_idx"), "disputes");
            log.info("Disputes indexes checked");

            IndexOperations resolutionIndexOps = mongoTemplate.indexOps("dispute_resolutions");
            ensureIndex(resolutionIndexOps, new Index()
                    .on("disputeId", Sort.Direction.ASC)
                    .on("tenantId", Sort.Direction.ASC)
                    .unique()
                    .named("disputeId_idx"), "dispute_resolutions");
            ensureIndex(resolutionIndexOps, new Index().on("decision", Sort.Direction.ASC).named("decision_idx"), "dispute_resolutions");
            ensureIndex(resolutionIndexOps, new Index().on("resolvedAt", Sort.Direction.DESC).named("resolvedAt_desc_idx"), "dispute_resolutions");
            ensureIndex(resolutionIndexOps, new Index().on("actionType", Sort.Direction.ASC).named("actionType_idx"), "dispute_resolutions");
            ensureIndex(resolutionIndexOps, new Index().on("appealed", Sort.Direction.ASC).named("appealed_idx"), "dispute_resolutions");
            log.info("Dispute resolutions indexes checked");

            IndexOperations appealIndexOps = mongoTemplate.indexOps("appeal_requests");
            ensureIndex(appealIndexOps, new Index().on("disputeId", Sort.Direction.ASC).named("disputeId_idx"), "appeal_requests");
            ensureIndex(appealIndexOps, new Index().on("status", Sort.Direction.ASC).named("status_idx"), "appeal_requests");
            ensureIndex(appealIndexOps, new Index().on("createdAt", Sort.Direction.DESC).named("createdAt_desc_idx"), "appeal_requests");
            log.info("Appeal request indexes checked");

            log.info("Reporting indexes initialization complete");
        } catch (Exception e) {
            log.error("Error initializing reporting indexes", e);
            throw new RuntimeException("Failed to initialize reporting indexes", e);
        }
    }

    private void ensureIndex(IndexOperations indexOperations, Index index, String collectionName) {
        try {
            indexOperations.ensureIndex(index);
        } catch (Exception e) {
            if (isExistingIndexConflict(e)) {
                log.warn("Skipping conflicting existing index on collection '{}': {}", collectionName, e.getMessage());
                return;
            }
            throw e;
        }
    }

    private boolean isExistingIndexConflict(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && (message.contains("IndexOptionsConflict")
                    || message.contains("already exists with a different name"))) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
