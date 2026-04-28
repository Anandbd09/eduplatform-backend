package com.eduplatform.security.config;

//import com.mongodb.MongoCommandException;
import javax.annotation.PostConstruct;

import com.mongodb.MongoCommandException;
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
public class CoreSecurityConfig {

    private final MongoTemplate mongoTemplate;

    public CoreSecurityConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initializeIndexes() {
        try {
            log.info("Initializing security indexes...");

            IndexOperations twoFaIndexOps = mongoTemplate.indexOps("two_factor_auth");
            ensureIndex(twoFaIndexOps, "two_factor_auth", new Index()
                    .on("userId", Sort.Direction.ASC)
                    .unique()
                    .named("userId_unique"));
            ensureIndex(twoFaIndexOps, "two_factor_auth", new Index()
                    .on("lastVerifiedAt", Sort.Direction.DESC)
                    .named("lastVerifiedAt_desc"));
            log.info("2FA indexes ensured");

            IndexOperations loginIndexOps = mongoTemplate.indexOps("login_attempts");
            ensureIndex(loginIndexOps, "login_attempts", new Index()
                    .on("userId", Sort.Direction.ASC)
                    .named("userId_idx"));
            ensureIndex(loginIndexOps, "login_attempts", new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"));
            ensureIndex(loginIndexOps, "login_attempts", new Index()
                    .on("attemptedAt", Sort.Direction.DESC)
                    .named("attemptedAt_desc"));
            ensureIndex(loginIndexOps, "login_attempts", new Index()
                    .on("email", Sort.Direction.ASC)
                    .named("email_idx"));
            log.info("Login attempt indexes ensured");

            IndexOperations auditIndexOps = mongoTemplate.indexOps("security_audit_logs");
            ensureIndex(auditIndexOps, "security_audit_logs", new Index()
                    .on("userId", Sort.Direction.ASC)
                    .named("userId_idx"));
            ensureIndex(auditIndexOps, "security_audit_logs", new Index()
                    .on("action", Sort.Direction.ASC)
                    .named("action_idx"));
            ensureIndex(auditIndexOps, "security_audit_logs", new Index()
                    .on("timestamp", Sort.Direction.DESC)
                    .named("timestamp_desc"));
            log.info("Security audit log indexes ensured");

            IndexOperations ipIndexOps = mongoTemplate.indexOps("ip_whitelists");
            ensureIndex(ipIndexOps, "ip_whitelists", new Index()
                    .on("userId", Sort.Direction.ASC)
                    .named("userId_idx"));
            ensureIndex(ipIndexOps, "ip_whitelists", new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"));
            ensureIndex(ipIndexOps, "ip_whitelists", new Index()
                    .on("createdAt", Sort.Direction.DESC)
                    .named("createdAt_desc"));
            log.info("IP whitelist indexes ensured");

            IndexOperations deviceIndexOps = mongoTemplate.indexOps("device_sessions");
            ensureIndex(deviceIndexOps, "device_sessions", new Index()
                    .on("userId", Sort.Direction.ASC)
                    .named("userId_idx"));
            ensureIndex(deviceIndexOps, "device_sessions", new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"));
            ensureIndex(deviceIndexOps, "device_sessions", new Index()
                    .on("createdAt", Sort.Direction.DESC)
                    .named("createdAt_desc"));
            ensureIndex(deviceIndexOps, "device_sessions", new Index()
                    .on("lastActivityAt", Sort.Direction.DESC)
                    .named("lastActivityAt_desc"));
            log.info("Device session indexes ensured");

            log.info("All security indexes initialized");
        } catch (Exception e) {
            log.error("Error initializing security indexes", e);
            throw new RuntimeException("Failed to initialize security indexes", e);
        }
    }

    private void ensureIndex(IndexOperations indexOperations, String collectionName, Index index) {
        try {
            indexOperations.ensureIndex(index);
        } catch (RuntimeException e) {
            if (isExistingIndexNameConflict(e)) {
                log.warn("Skipping index creation for collection={} because an equivalent index already exists with a different name", collectionName);
                return;
            }
            throw e;
        }
    }

    private boolean isExistingIndexNameConflict(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof MongoCommandException mongoException
                    && mongoException.getErrorCode() == 85
                    && mongoException.getErrorMessage() != null
                    && mongoException.getErrorMessage().contains("Index already exists with a different name")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
