package com.eduplatform.batch.config;

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
public class BatchConfig {

    private final MongoTemplate mongoTemplate;

    public BatchConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initializeIndexes() {
        try {
            log.info("Initializing Batch Operations indexes...");

            IndexOperations jobIndexOps = mongoTemplate.indexOps("batch_jobs");
            ensureIndex(jobIndexOps, new Index()
                    .on("jobId", Sort.Direction.ASC)
                    .unique()
                    .named("jobId_unique"), "batch_jobs");
            ensureIndex(jobIndexOps, new Index()
                    .on("userId", Sort.Direction.ASC)
                    .on("tenantId", Sort.Direction.ASC)
                    .named("userId_tenantId_idx"), "batch_jobs");
            ensureIndex(jobIndexOps, new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"), "batch_jobs");
            ensureIndex(jobIndexOps, new Index()
                    .on("createdAt", Sort.Direction.DESC)
                    .named("createdAt_desc_idx"), "batch_jobs");
            log.info("Batch Job indexes checked");

            IndexOperations resultIndexOps = mongoTemplate.indexOps("batch_job_results");
            ensureIndex(resultIndexOps, new Index()
                    .on("jobId", Sort.Direction.ASC)
                    .named("jobId_idx"), "batch_job_results");
            ensureIndex(resultIndexOps, new Index()
                    .on("recordNumber", Sort.Direction.ASC)
                    .named("recordNumber_idx"), "batch_job_results");
            ensureIndex(resultIndexOps, new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"), "batch_job_results");
            log.info("Batch Result indexes checked");

            IndexOperations importIndexOps = mongoTemplate.indexOps("user_imports");
            ensureIndex(importIndexOps, new Index()
                    .on("jobId", Sort.Direction.ASC)
                    .named("jobId_idx"), "user_imports");
            ensureIndex(importIndexOps, new Index()
                    .on("importType", Sort.Direction.ASC)
                    .named("importType_idx"), "user_imports");
            ensureIndex(importIndexOps, new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"), "user_imports");
            log.info("User Import indexes checked");

            IndexOperations assignmentIndexOps = mongoTemplate.indexOps("course_assignments");
            ensureIndex(assignmentIndexOps, new Index()
                    .on("jobId", Sort.Direction.ASC)
                    .named("jobId_idx"), "course_assignments");
            ensureIndex(assignmentIndexOps, new Index()
                    .on("courseId", Sort.Direction.ASC)
                    .named("courseId_idx"), "course_assignments");
            ensureIndex(assignmentIndexOps, new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"), "course_assignments");
            log.info("Course Assignment indexes checked");

            IndexOperations enrollmentIndexOps = mongoTemplate.indexOps("enrollment_batches");
            ensureIndex(enrollmentIndexOps, new Index()
                    .on("jobId", Sort.Direction.ASC)
                    .named("jobId_idx"), "enrollment_batches");
            ensureIndex(enrollmentIndexOps, new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"), "enrollment_batches");
            log.info("Enrollment Batch indexes checked");

            IndexOperations auditIndexOps = mongoTemplate.indexOps("batch_audit_logs");
            ensureIndex(auditIndexOps, new Index()
                    .on("jobId", Sort.Direction.ASC)
                    .named("jobId_idx"), "batch_audit_logs");
            ensureIndex(auditIndexOps, new Index()
                    .on("action", Sort.Direction.ASC)
                    .named("action_idx"), "batch_audit_logs");
            ensureIndex(auditIndexOps, new Index()
                    .on("timestamp", Sort.Direction.DESC)
                    .named("timestamp_desc_idx"), "batch_audit_logs");
            log.info("Batch Audit Log indexes checked");

            log.info("Batch Operations index initialization complete");
        } catch (Exception e) {
            log.error("Error initializing batch indexes", e);
            throw new RuntimeException("Failed to initialize batch indexes", e);
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
