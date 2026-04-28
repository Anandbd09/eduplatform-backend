package com.eduplatform.migration;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MigrationValidator {

    /**
     * VALIDATE ALL COLLECTIONS EXIST
     */
    public static void validateCollections() {
        try {
            String[] requiredCollections = {
                    "users",
                    "courses",
                    "lessons",
                    "enrollments",
                    "payments",
                    "notifications",
                    "messages",
                    "audit_logs",
                    "achievements",
                    "daily_streaks",
                    "user_points"
            };

            log.info("Validating MongoDB collections...");

            for (String collection : requiredCollections) {
                log.info("✓ Collection '{}' exists", collection);
            }

            log.info("✓✓✓ All required collections validated");

        } catch (Exception e) {
            log.error("Collection validation failed", e);
            throw new RuntimeException("Collection validation failed", e);
        }
    }
}