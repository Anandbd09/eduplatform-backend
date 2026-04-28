package com.eduplatform.cache.config;

import com.mongodb.MongoCommandException;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@Configuration
@EnableTransactionManagement
@EnableCaching
public class CacheConfig {

    private final MongoTemplate mongoTemplate;

    public CacheConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initializeIndexes() {
        try {
            log.info("Initializing cache indexes...");

            IndexOperations cacheIndexOps = mongoTemplate.indexOps("cache_entries");
            ensureIndex(cacheIndexOps, "cache_entries", new Index()
                    .on("cacheKey", Sort.Direction.ASC)
                    .unique()
                    .named("cacheKey_unique"));
            ensureIndex(cacheIndexOps, "cache_entries", new Index()
                    .on("cacheType", Sort.Direction.ASC)
                    .on("tenantId", Sort.Direction.ASC)
                    .named("cacheType_tenantId_idx"));
            ensureIndex(cacheIndexOps, "cache_entries", new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"));
            ensureIndex(cacheIndexOps, "cache_entries", new Index()
                    .on("expiresAt", Sort.Direction.ASC)
                    .named("expiresAt_idx"));
            ensureIndex(cacheIndexOps, "cache_entries", new Index()
                    .on("lastAccessedAt", Sort.Direction.DESC)
                    .named("lastAccessedAt_desc"));
            log.info("Cache entry indexes ensured");

            IndexOperations statsIndexOps = mongoTemplate.indexOps("cache_statistics");
            ensureIndex(statsIndexOps, "cache_statistics", new Index()
                    .on("cacheType", Sort.Direction.ASC)
                    .unique()
                    .named("cacheType_unique"));
            ensureIndex(statsIndexOps, "cache_statistics", new Index()
                    .on("tenantId", Sort.Direction.ASC)
                    .named("tenantId_idx"));
            log.info("Cache statistics indexes ensured");

            IndexOperations policyIndexOps = mongoTemplate.indexOps("cache_policies");
            ensureIndex(policyIndexOps, "cache_policies", new Index()
                    .on("cacheType", Sort.Direction.ASC)
                    .unique()
                    .named("cacheType_unique"));
            ensureIndex(policyIndexOps, "cache_policies", new Index()
                    .on("tenantId", Sort.Direction.ASC)
                    .named("tenantId_idx"));
            ensureIndex(policyIndexOps, "cache_policies", new Index()
                    .on("enabled", Sort.Direction.ASC)
                    .named("enabled_idx"));
            ensureIndex(policyIndexOps, "cache_policies", new Index()
                    .on("evictionPolicy", Sort.Direction.ASC)
                    .named("evictionPolicy_idx"));
            log.info("Cache policy indexes ensured");

            log.info("All cache indexes initialized");
        } catch (Exception e) {
            log.error("Error initializing cache indexes", e);
            throw new RuntimeException("Failed to initialize cache indexes", e);
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
