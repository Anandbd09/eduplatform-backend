package com.eduplatform.review.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableTransactionManagement
public class ReviewConfig {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void initializeReviewIndexes() {
        try {
            log.info("Initializing Review collection indexes...");
            IndexOperations reviewIndexOps = mongoTemplate.indexOps("reviews");

            Index statusIndex = new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx");
            ensureIndex(reviewIndexOps, statusIndex, "reviews");
            log.info("Created index: status");

            Index helpfulCountIndex = new Index()
                    .on("helpfulCount", Sort.Direction.DESC)
                    .named("helpfulCount_desc_idx");
            ensureIndex(reviewIndexOps, helpfulCountIndex, "reviews");
            log.info("Created index: helpfulCount (DESC)");

            log.info("Review collection indexes initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing review indexes", e);
            throw new RuntimeException("Failed to initialize review indexes", e);
        }
    }

    @PostConstruct
    public void initializeRatingIndexes() {
        try {
            log.info("Initializing Rating collection indexes...");
            IndexOperations ratingIndexOps = mongoTemplate.indexOps("ratings");

            Index avgRatingIndex = new Index()
                    .on("averageRating", Sort.Direction.DESC)
                    .named("averageRating_desc_idx");
            ensureIndex(ratingIndexOps, avgRatingIndex, "ratings");
            log.info("Created index: averageRating (DESC)");

            log.info("Rating collection indexes initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing rating indexes", e);
            throw new RuntimeException("Failed to initialize rating indexes", e);
        }
    }

    @PostConstruct
    public void initializeReviewHelpfulIndexes() {
        try {
            log.info("Initializing ReviewHelpful collection indexes...");
            IndexOperations helpfulIndexOps = mongoTemplate.indexOps("review_helpful");

            Index isHelpfulIndex = new Index()
                    .on("isHelpful", Sort.Direction.ASC)
                    .named("isHelpful_idx");
            ensureIndex(helpfulIndexOps, isHelpfulIndex, "review_helpful");
            log.info("Created index: isHelpful");

            log.info("ReviewHelpful collection indexes initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing review helpful indexes", e);
            throw new RuntimeException("Failed to initialize review helpful indexes", e);
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
