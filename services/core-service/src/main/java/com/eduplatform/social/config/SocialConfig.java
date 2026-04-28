package com.eduplatform.social.config;

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
public class SocialConfig {

    private final MongoTemplate mongoTemplate;

    public SocialConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initializeIndexes() {
        try {
            log.info("Initializing social indexes...");

            IndexOperations followIndexOps = mongoTemplate.indexOps("user_follows");
            ensureIndex(followIndexOps, new Index().on("followerId", Sort.Direction.ASC).on("status", Sort.Direction.ASC).named("followerId_status_idx"), "user_follows");
            ensureIndex(followIndexOps, new Index().on("followingId", Sort.Direction.ASC).on("status", Sort.Direction.ASC).named("followingId_status_idx"), "user_follows");
            ensureIndex(followIndexOps, new Index().on("relationshipKey", Sort.Direction.ASC).unique().named("relationshipKey_unique"), "user_follows");
            log.info("User follow indexes checked");

            IndexOperations msgIndexOps = mongoTemplate.indexOps("messages");
            ensureIndex(msgIndexOps, new Index().on("senderId", Sort.Direction.ASC).named("senderId_idx"), "messages");
            ensureIndex(msgIndexOps, new Index().on("recipientId", Sort.Direction.ASC).on("status", Sort.Direction.ASC).named("recipientId_status_idx"), "messages");
            ensureIndex(msgIndexOps, new Index().on("sentAt", Sort.Direction.DESC).named("sentAt_desc"), "messages");
            log.info("Message indexes checked");

            IndexOperations threadIndexOps = mongoTemplate.indexOps("forum_threads");
            ensureIndex(threadIndexOps, new Index().on("courseId", Sort.Direction.ASC).on("status", Sort.Direction.ASC).named("courseId_status_idx"), "forum_threads");
            ensureIndex(threadIndexOps, new Index().on("creatorId", Sort.Direction.ASC).named("creatorId_idx"), "forum_threads");
            ensureIndex(threadIndexOps, new Index().on("category", Sort.Direction.ASC).named("category_idx"), "forum_threads");
            ensureIndex(threadIndexOps, new Index().on("createdAt", Sort.Direction.DESC).named("createdAt_desc"), "forum_threads");
            log.info("Forum thread indexes checked");

            IndexOperations postIndexOps = mongoTemplate.indexOps("forum_posts");
            ensureIndex(postIndexOps, new Index().on("threadId", Sort.Direction.ASC).named("threadId_idx"), "forum_posts");
            ensureIndex(postIndexOps, new Index().on("authorId", Sort.Direction.ASC).named("authorId_idx"), "forum_posts");
            ensureIndex(postIndexOps, new Index().on("createdAt", Sort.Direction.ASC).named("createdAt_asc"), "forum_posts");
            log.info("Forum post indexes checked");

            IndexOperations likeIndexOps = mongoTemplate.indexOps("likes");
            ensureIndex(likeIndexOps, new Index().on("userId", Sort.Direction.ASC).named("userId_idx"), "likes");
            ensureIndex(likeIndexOps, new Index().on("contentId", Sort.Direction.ASC).named("contentId_idx"), "likes");
            ensureIndex(likeIndexOps, new Index().on("likeKey", Sort.Direction.ASC).unique().named("likeKey_unique"), "likes");
            log.info("Like indexes checked");

            log.info("Social index initialization complete");
        } catch (Exception e) {
            log.error("Error initializing social indexes", e);
            throw new RuntimeException("Failed to initialize social indexes", e);
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
