package com.eduplatform.recommendation.config;

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
public class RecommendationConfig {

    private final MongoTemplate mongoTemplate;

    public RecommendationConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initializeIndexes() {
        try {
            log.info("Initializing Recommendation Engine indexes...");

            IndexOperations userPrefIndexOps = mongoTemplate.indexOps("user_preferences");
            ensureIndex(userPrefIndexOps, new Index()
                    .on("userId", Sort.Direction.ASC)
                    .on("courseId", Sort.Direction.ASC)
                    .on("tenantId", Sort.Direction.ASC)
                    .unique()
                    .named("userId_courseId_tenantId_unique"), "user_preferences");
            ensureIndex(userPrefIndexOps, new Index()
                    .on("userId", Sort.Direction.ASC)
                    .named("userId_idx"), "user_preferences");
            ensureIndex(userPrefIndexOps, new Index()
                    .on("interactionDate", Sort.Direction.DESC)
                    .named("interactionDate_desc_idx"), "user_preferences");
            log.info("UserPreference indexes checked");

            IndexOperations courseVectorIndexOps = mongoTemplate.indexOps("course_vectors");
            ensureIndex(courseVectorIndexOps, new Index()
                    .on("courseId", Sort.Direction.ASC)
                    .unique()
                    .named("courseId_unique"), "course_vectors");
            ensureIndex(courseVectorIndexOps, new Index()
                    .on("category", Sort.Direction.ASC)
                    .named("category_idx"), "course_vectors");
            ensureIndex(courseVectorIndexOps, new Index()
                    .on("rating", Sort.Direction.DESC)
                    .named("rating_desc_idx"), "course_vectors");
            log.info("CourseVector indexes checked");

            IndexOperations recIndexOps = mongoTemplate.indexOps("recommendations");
            ensureIndex(recIndexOps, new Index()
                    .on("userId", Sort.Direction.ASC)
                    .on("courseId", Sort.Direction.ASC)
                    .on("tenantId", Sort.Direction.ASC)
                    .named("userId_courseId_tenantId_idx"), "recommendations");
            ensureIndex(recIndexOps, new Index()
                    .on("score", Sort.Direction.DESC)
                    .named("score_desc_idx"), "recommendations");
            ensureIndex(recIndexOps, new Index()
                    .on("clicked", Sort.Direction.ASC)
                    .named("clicked_idx"), "recommendations");
            log.info("RecommendationRecord indexes checked");

            IndexOperations simIndexOps = mongoTemplate.indexOps("course_similarities");
            ensureIndex(simIndexOps, new Index()
                    .on("sourceCourseId", Sort.Direction.ASC)
                    .on("targetCourseId", Sort.Direction.ASC)
                    .unique()
                    .named("sourceCourseId_targetCourseId_unique"), "course_similarities");
            ensureIndex(simIndexOps, new Index()
                    .on("similarityScore", Sort.Direction.DESC)
                    .named("similarityScore_desc_idx"), "course_similarities");
            log.info("CourseSimilarity indexes checked");

            IndexOperations cacheIndexOps = mongoTemplate.indexOps("recommendation_cache");
            ensureIndex(cacheIndexOps, new Index()
                    .on("userId", Sort.Direction.ASC)
                    .unique()
                    .named("userId_unique"), "recommendation_cache");
            ensureIndex(cacheIndexOps, new Index()
                    .on("expiresAt", Sort.Direction.ASC)
                    .named("expiresAt_idx"), "recommendation_cache");
            log.info("RecommendationCache indexes checked");

            log.info("Recommendation Engine index initialization complete");
        } catch (Exception e) {
            log.error("Error initializing recommendation indexes", e);
            throw new RuntimeException("Failed to initialize recommendation indexes", e);
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
