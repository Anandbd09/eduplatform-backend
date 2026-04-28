package com.eduplatform.gamification.config;

import com.eduplatform.gamification.model.Achievement;
import com.eduplatform.gamification.model.DailyStreak;
import com.eduplatform.gamification.model.UserAchievement;
import com.eduplatform.gamification.model.UserPoints;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class GamificationConfig {

    private static final Logger log = LoggerFactory.getLogger(GamificationConfig.class);

    private final MongoTemplate mongoTemplate;

    public GamificationConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initializeIndexes() {
        try {
            log.info("Initializing gamification indexes...");

            initializeDailyStreakIndexes();
            initializeUserPointsIndexes();
            initializeAchievementIndexes();
            initializeUserAchievementIndexes();

            log.info("Gamification indexes initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing gamification indexes", e);
            throw new RuntimeException("Failed to initialize gamification indexes", e);
        }
    }

    private void initializeDailyStreakIndexes() {
        IndexOperations indexOperations = mongoTemplate.indexOps(DailyStreak.class);

        ensureIndex(indexOperations, new Index()
                .on("userId", Sort.Direction.ASC)
                .on("courseId", Sort.Direction.ASC)
                .on("tenantId", Sort.Direction.ASC)
                .named("user_course_tenant_idx"), "daily_streaks");

        ensureIndex(indexOperations, new Index()
                .on("userId", Sort.Direction.ASC)
                .on("tenantId", Sort.Direction.ASC)
                .named("user_tenant_idx"), "daily_streaks");

        ensureIndex(indexOperations, new Index()
                .on("isActive", Sort.Direction.ASC)
                .on("tenantId", Sort.Direction.ASC)
                .named("active_tenant_idx"), "daily_streaks");
    }

    private void initializeUserPointsIndexes() {
        IndexOperations indexOperations = mongoTemplate.indexOps(UserPoints.class);

        ensureIndex(indexOperations, new Index()
                .on("userId", Sort.Direction.ASC)
                .on("tenantId", Sort.Direction.ASC)
                .unique()
                .named("user_tenant_unique_idx"), "user_points");

        ensureIndex(indexOperations, new Index()
                .on("tenantId", Sort.Direction.ASC)
                .on("totalPoints", Sort.Direction.DESC)
                .named("tenant_total_points_desc_idx"), "user_points");

        ensureIndex(indexOperations, new Index()
                .on("tenantId", Sort.Direction.ASC)
                .on("currentLevel", Sort.Direction.ASC)
                .named("tenant_current_level_idx"), "user_points");
    }

    private void initializeAchievementIndexes() {
        IndexOperations indexOperations = mongoTemplate.indexOps(Achievement.class);

        ensureIndex(indexOperations, new Index()
                .on("achievementCode", Sort.Direction.ASC)
                .on("tenantId", Sort.Direction.ASC)
                .named("achievement_code_tenant_idx"), "achievements");

        ensureIndex(indexOperations, new Index()
                .on("tenantId", Sort.Direction.ASC)
                .named("tenant_idx"), "achievements");
    }

    private void initializeUserAchievementIndexes() {
        IndexOperations indexOperations = mongoTemplate.indexOps(UserAchievement.class);

        ensureIndex(indexOperations, new Index()
                .on("userId", Sort.Direction.ASC)
                .on("isUnlocked", Sort.Direction.ASC)
                .on("tenantId", Sort.Direction.ASC)
                .named("user_unlocked_tenant_idx"), "user_achievements");

        ensureIndex(indexOperations, new Index()
                .on("userId", Sort.Direction.ASC)
                .on("tenantId", Sort.Direction.ASC)
                .named("user_tenant_idx"), "user_achievements");
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
