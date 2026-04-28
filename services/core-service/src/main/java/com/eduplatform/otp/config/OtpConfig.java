package com.eduplatform.otp.config;

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
public class OtpConfig {

    private final MongoTemplate mongoTemplate;

    public OtpConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initializeIndexes() {
        try {
            log.info("Initializing OTP indexes...");

            IndexOperations otpIndexOps = mongoTemplate.indexOps("otp_codes");
            ensureIndex(otpIndexOps, new Index().on("userId", Sort.Direction.ASC).named("userId_idx"), "otp_codes");
            ensureIndex(otpIndexOps, new Index().on("status", Sort.Direction.ASC).named("status_idx"), "otp_codes");
            ensureIndex(otpIndexOps, new Index().on("expiresAt", Sort.Direction.ASC).named("expiresAt_idx"), "otp_codes");
            ensureIndex(otpIndexOps, new Index().on("createdAt", Sort.Direction.DESC).named("createdAt_desc"), "otp_codes");
            log.info("OTP code indexes checked");

            IndexOperations phoneIndexOps = mongoTemplate.indexOps("phone_verifications");
            ensureIndex(phoneIndexOps, new Index().on("userId", Sort.Direction.ASC).unique().named("userId_unique"), "phone_verifications");
            ensureIndex(phoneIndexOps, new Index().on("status", Sort.Direction.ASC).named("status_idx"), "phone_verifications");
            log.info("Phone verification indexes checked");

            IndexOperations emailIndexOps = mongoTemplate.indexOps("email_verifications");
            ensureIndex(emailIndexOps, new Index().on("userId", Sort.Direction.ASC).unique().named("userId_unique"), "email_verifications");
            ensureIndex(emailIndexOps, new Index().on("status", Sort.Direction.ASC).named("status_idx"), "email_verifications");
            log.info("Email verification indexes checked");

            log.info("OTP index initialization complete");
        } catch (Exception e) {
            log.error("Error initializing OTP indexes", e);
            throw new RuntimeException("Failed to initialize OTP indexes", e);
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
