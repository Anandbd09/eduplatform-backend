package com.eduplatform.referral.config;

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
public class ReferralConfig {

    private final MongoTemplate mongoTemplate;

    public ReferralConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initializeIndexes() {
        try {
            log.info("Initializing Referral System indexes...");

            IndexOperations codeIndexOps = mongoTemplate.indexOps("referral_codes");
            ensureIndex(codeIndexOps, new Index()
                    .on("referralCode", Sort.Direction.ASC)
                    .unique()
                    .named("referralCode_unique"), "referral_codes");
            ensureIndex(codeIndexOps, new Index()
                    .on("instructorId", Sort.Direction.ASC)
                    .on("tenantId", Sort.Direction.ASC)
                    .named("instructorId_tenantId_idx"), "referral_codes");
            ensureIndex(codeIndexOps, new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"), "referral_codes");
            ensureIndex(codeIndexOps, new Index()
                    .on("createdAt", Sort.Direction.DESC)
                    .named("createdAt_desc_idx"), "referral_codes");
            log.info("Referral Code indexes checked");

            IndexOperations clickIndexOps = mongoTemplate.indexOps("referral_clicks");
            ensureIndex(clickIndexOps, new Index()
                    .on("referralCode", Sort.Direction.ASC)
                    .on("clickedAt", Sort.Direction.ASC)
                    .named("referralCode_clickedAt_idx"), "referral_clicks");
            ensureIndex(clickIndexOps, new Index()
                    .on("instructorId", Sort.Direction.ASC)
                    .named("instructorId_idx"), "referral_clicks");
            ensureIndex(clickIndexOps, new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"), "referral_clicks");
            ensureIndex(clickIndexOps, new Index()
                    .on("clickedAt", Sort.Direction.DESC)
                    .named("clickedAt_desc_idx"), "referral_clicks");
            log.info("Referral Click indexes checked");

            IndexOperations rewardIndexOps = mongoTemplate.indexOps("referral_rewards");
            ensureIndex(rewardIndexOps, new Index()
                    .on("instructorId", Sort.Direction.ASC)
                    .on("expiresAt", Sort.Direction.ASC)
                    .named("instructorId_expiresAt_idx"), "referral_rewards");
            ensureIndex(rewardIndexOps, new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"), "referral_rewards");
            ensureIndex(rewardIndexOps, new Index()
                    .on("expiresAt", Sort.Direction.ASC)
                    .named("expiresAt_idx"), "referral_rewards");
            ensureIndex(rewardIndexOps, new Index()
                    .on("createdAt", Sort.Direction.DESC)
                    .named("createdAt_desc_idx"), "referral_rewards");
            log.info("Referral Reward indexes checked");

            IndexOperations payoutIndexOps = mongoTemplate.indexOps("referral_payouts");
            ensureIndex(payoutIndexOps, new Index()
                    .on("instructorId", Sort.Direction.ASC)
                    .named("instructorId_idx"), "referral_payouts");
            ensureIndex(payoutIndexOps, new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"), "referral_payouts");
            log.info("Referral Payout indexes checked");

            IndexOperations analyticsIndexOps = mongoTemplate.indexOps("referral_analytics");
            ensureIndex(analyticsIndexOps, new Index()
                    .on("referralCodeId", Sort.Direction.ASC)
                    .unique()
                    .named("referralCodeId_unique"), "referral_analytics");
            log.info("Referral Analytics indexes checked");

            log.info("Referral System index initialization complete");
        } catch (Exception e) {
            log.error("Error initializing referral indexes", e);
            throw new RuntimeException("Failed to initialize referral indexes", e);
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
