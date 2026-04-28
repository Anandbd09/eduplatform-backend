package com.eduplatform.monitoring.config;

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
public class MonitoringConfig {

    private final MongoTemplate mongoTemplate;

    public MonitoringConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initializeIndexes() {
        try {
            log.info("Initializing monitoring indexes...");

            IndexOperations logIndexOps = mongoTemplate.indexOps("application_logs");
            ensureIndex(logIndexOps, new Index().on("level", Sort.Direction.ASC).named("level_idx"), "application_logs");
            ensureIndex(logIndexOps, new Index().on("category", Sort.Direction.ASC).named("category_idx"), "application_logs");
            ensureIndex(logIndexOps, new Index().on("timestamp", Sort.Direction.DESC).named("timestamp_desc"), "application_logs");
            ensureIndex(logIndexOps, new Index().on("userId", Sort.Direction.ASC).named("userId_idx"), "application_logs");
            log.info("Application log indexes checked");

            IndexOperations metricIndexOps = mongoTemplate.indexOps("performance_metrics");
            ensureIndex(metricIndexOps, new Index().on("endpoint", Sort.Direction.ASC).named("endpoint_idx"), "performance_metrics");
            ensureIndex(metricIndexOps, new Index().on("timestamp", Sort.Direction.DESC).named("timestamp_desc"), "performance_metrics");
            ensureIndex(metricIndexOps, new Index().on("tenantId", Sort.Direction.ASC).named("tenantId_idx"), "performance_metrics");
            log.info("Performance metric indexes checked");

            IndexOperations alertIndexOps = mongoTemplate.indexOps("system_alerts");
            ensureIndex(alertIndexOps, new Index().on("status", Sort.Direction.ASC).named("status_idx"), "system_alerts");
            ensureIndex(alertIndexOps, new Index().on("severity", Sort.Direction.ASC).named("severity_idx"), "system_alerts");
            ensureIndex(alertIndexOps, new Index().on("alertType", Sort.Direction.ASC).named("alertType_idx"), "system_alerts");
            ensureIndex(alertIndexOps, new Index().on("createdAt", Sort.Direction.DESC).named("createdAt_desc"), "system_alerts");
            ensureIndex(alertIndexOps, new Index().on("tenantId", Sort.Direction.ASC).named("tenantId_idx"), "system_alerts");
            log.info("System alert indexes checked");

            IndexOperations dashboardIndexOps = mongoTemplate.indexOps("monitoring_dashboards");
            ensureIndex(dashboardIndexOps, new Index()
                    .on("dashboardName", Sort.Direction.ASC)
                    .unique()
                    .named("dashboardName_unique"), "monitoring_dashboards");
            ensureIndex(dashboardIndexOps, new Index().on("tenantId", Sort.Direction.ASC).named("tenantId_idx"), "monitoring_dashboards");
            ensureIndex(dashboardIndexOps, new Index().on("lastUpdatedAt", Sort.Direction.DESC).named("lastUpdatedAt_desc"), "monitoring_dashboards");
            log.info("Monitoring dashboard indexes checked");

            log.info("Monitoring index initialization complete");
        } catch (Exception e) {
            log.error("Error initializing monitoring indexes", e);
            throw new RuntimeException("Failed to initialize monitoring indexes", e);
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
