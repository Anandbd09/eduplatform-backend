package com.eduplatform.export.config;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@Configuration
@EnableTransactionManagement
@EnableAsync
public class ExportConfig {

    private final MongoTemplate mongoTemplate;

    public ExportConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initializeIndexes() {
        try {
            log.info("Initializing export indexes...");

            IndexOperations jobIndexOps = mongoTemplate.indexOps("export_jobs");
            ensureIndex(jobIndexOps, new Index()
                    .on("jobId", Sort.Direction.ASC)
                    .unique()
                    .named("jobId_unique"), "export_jobs");
            ensureIndex(jobIndexOps, new Index()
                    .on("userId", Sort.Direction.ASC)
                    .named("userId_idx"), "export_jobs");
            ensureIndex(jobIndexOps, new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"), "export_jobs");
            ensureIndex(jobIndexOps, new Index()
                    .on("expiresAt", Sort.Direction.ASC)
                    .named("expiresAt_idx"), "export_jobs");
            log.info("Export job indexes checked");

            IndexOperations templateIndexOps = mongoTemplate.indexOps("export_templates");
            ensureIndex(templateIndexOps, new Index()
                    .on("templateName", Sort.Direction.ASC)
                    .named("templateName_idx"), "export_templates");
            ensureIndex(templateIndexOps, new Index()
                    .on("createdBy", Sort.Direction.ASC)
                    .named("createdBy_idx"), "export_templates");
            ensureIndex(templateIndexOps, new Index()
                    .on("isPublic", Sort.Direction.ASC)
                    .named("isPublic_idx"), "export_templates");
            log.info("Export template indexes checked");

            IndexOperations auditIndexOps = mongoTemplate.indexOps("export_audit_logs");
            ensureIndex(auditIndexOps, new Index()
                    .on("jobId", Sort.Direction.ASC)
                    .named("jobId_idx"), "export_audit_logs");
            ensureIndex(auditIndexOps, new Index()
                    .on("userId", Sort.Direction.ASC)
                    .named("userId_idx"), "export_audit_logs");
            ensureIndex(auditIndexOps, new Index()
                    .on("action", Sort.Direction.ASC)
                    .named("action_idx"), "export_audit_logs");
            ensureIndex(auditIndexOps, new Index()
                    .on("timestamp", Sort.Direction.DESC)
                    .named("timestamp_desc"), "export_audit_logs");
            log.info("Export audit log indexes checked");

            IndexOperations formatIndexOps = mongoTemplate.indexOps("export_formats");
            ensureIndex(formatIndexOps, new Index()
                    .on("formatName", Sort.Direction.ASC)
                    .named("formatName_idx"), "export_formats");
            log.info("Export format indexes checked");

            log.info("Export index initialization complete");
        } catch (Exception e) {
            log.error("Error initializing export indexes", e);
            throw new RuntimeException("Failed to initialize export indexes", e);
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
