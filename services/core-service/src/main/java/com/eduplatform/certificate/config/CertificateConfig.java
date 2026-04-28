package com.eduplatform.certificate.config;

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
public class CertificateConfig {

    private final MongoTemplate mongoTemplate;

    public CertificateConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initializeIndexes() {
        try {
            log.info("Initializing Certificate indexes...");

            IndexOperations certIndexOps = mongoTemplate.indexOps("certificates");
            ensureIndex(certIndexOps, new Index()
                    .on("userId", Sort.Direction.ASC)
                    .on("courseId", Sort.Direction.ASC)
                    .on("tenantId", Sort.Direction.ASC)
                    .unique()
                    .named("userId_courseId_tenantId_unique"), "certificates");
            ensureIndex(certIndexOps, new Index()
                    .on("certificateNumber", Sort.Direction.ASC)
                    .unique()
                    .named("certificateNumber_unique"), "certificates");
            ensureIndex(certIndexOps, new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"), "certificates");
            ensureIndex(certIndexOps, new Index()
                    .on("issuedAt", Sort.Direction.DESC)
                    .named("issuedAt_desc_idx"), "certificates");
            log.info("Certificate indexes checked");

            IndexOperations templateIndexOps = mongoTemplate.indexOps("certificate_templates");
            ensureIndex(templateIndexOps, new Index()
                    .on("name", Sort.Direction.ASC)
                    .unique()
                    .named("name_unique"), "certificate_templates");
            ensureIndex(templateIndexOps, new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"), "certificate_templates");
            ensureIndex(templateIndexOps, new Index()
                    .on("createdAt", Sort.Direction.DESC)
                    .named("createdAt_desc_idx"), "certificate_templates");
            log.info("Certificate template indexes checked");

            IndexOperations verificationIndexOps = mongoTemplate.indexOps("certificate_verifications");
            ensureIndex(verificationIndexOps, new Index()
                    .on("certificateNumber", Sort.Direction.ASC)
                    .unique()
                    .named("certificateNumber_unique"), "certificate_verifications");
            ensureIndex(verificationIndexOps, new Index()
                    .on("verificationStatus", Sort.Direction.ASC)
                    .named("verificationStatus_idx"), "certificate_verifications");
            ensureIndex(verificationIndexOps, new Index()
                    .on("verifiedAt", Sort.Direction.DESC)
                    .named("verifiedAt_desc_idx"), "certificate_verifications");
            log.info("Certificate verification indexes checked");

            IndexOperations revocationIndexOps = mongoTemplate.indexOps("certificate_revocations");
            ensureIndex(revocationIndexOps, new Index()
                    .on("certificateNumber", Sort.Direction.ASC)
                    .named("certificateNumber_idx"), "certificate_revocations");
            ensureIndex(revocationIndexOps, new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"), "certificate_revocations");
            ensureIndex(revocationIndexOps, new Index()
                    .on("reason", Sort.Direction.ASC)
                    .named("reason_idx"), "certificate_revocations");
            ensureIndex(revocationIndexOps, new Index()
                    .on("revokedAt", Sort.Direction.DESC)
                    .named("revokedAt_desc_idx"), "certificate_revocations");
            log.info("Certificate revocation indexes checked");

            IndexOperations badgeIndexOps = mongoTemplate.indexOps("certificate_badges");
            ensureIndex(badgeIndexOps, new Index()
                    .on("certificateId", Sort.Direction.ASC)
                    .named("certificateId_idx"), "certificate_badges");
            ensureIndex(badgeIndexOps, new Index()
                    .on("isPublic", Sort.Direction.ASC)
                    .named("isPublic_idx"), "certificate_badges");
            log.info("Certificate badge indexes checked");

            log.info("Certificate index initialization complete");
        } catch (Exception e) {
            log.error("Error initializing certificate indexes", e);
            throw new RuntimeException("Failed to initialize certificate indexes", e);
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
