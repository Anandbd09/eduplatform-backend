package com.eduplatform.coupon.config;

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
public class CouponConfig {

    private final MongoTemplate mongoTemplate;

    public CouponConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initializeIndexes() {
        try {
            log.info("Initializing Coupon & Promotions indexes...");

            IndexOperations couponIndexOps = mongoTemplate.indexOps("coupons");
            ensureIndex(couponIndexOps, new Index()
                    .on("code", Sort.Direction.ASC)
                    .on("tenantId", Sort.Direction.ASC)
                    .unique()
                    .named("code_tenantId_unique"), "coupons");
            ensureIndex(couponIndexOps, new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"), "coupons");
            ensureIndex(couponIndexOps, new Index()
                    .on("validFrom", Sort.Direction.ASC)
                    .on("validUntil", Sort.Direction.ASC)
                    .named("validity_idx"), "coupons");
            ensureIndex(couponIndexOps, new Index()
                    .on("createdAt", Sort.Direction.DESC)
                    .named("createdAt_desc_idx"), "coupons");
            log.info("Coupon indexes checked");

            IndexOperations codeIndexOps = mongoTemplate.indexOps("coupon_codes");
            ensureIndex(codeIndexOps, new Index()
                    .on("code", Sort.Direction.ASC)
                    .unique()
                    .named("code_unique"), "coupon_codes");
            ensureIndex(codeIndexOps, new Index()
                    .on("couponId", Sort.Direction.ASC)
                    .named("couponId_idx"), "coupon_codes");
            ensureIndex(codeIndexOps, new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"), "coupon_codes");
            log.info("CouponCode indexes checked");

            IndexOperations redemptionIndexOps = mongoTemplate.indexOps("coupon_redemptions");
            ensureIndex(redemptionIndexOps, new Index()
                    .on("userId", Sort.Direction.ASC)
                    .on("couponId", Sort.Direction.ASC)
                    .on("tenantId", Sort.Direction.ASC)
                    .named("userId_couponId_tenantId_idx"), "coupon_redemptions");
            ensureIndex(redemptionIndexOps, new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"), "coupon_redemptions");
            ensureIndex(redemptionIndexOps, new Index()
                    .on("redeemedAt", Sort.Direction.DESC)
                    .named("redeemedAt_desc_idx"), "coupon_redemptions");
            log.info("CouponRedemption indexes checked");

            IndexOperations campaignIndexOps = mongoTemplate.indexOps("promotion_campaigns");
            ensureIndex(campaignIndexOps, new Index()
                    .on("campaignCode", Sort.Direction.ASC)
                    .unique()
                    .named("campaignCode_unique"), "promotion_campaigns");
            ensureIndex(campaignIndexOps, new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"), "promotion_campaigns");
            ensureIndex(campaignIndexOps, new Index()
                    .on("startDate", Sort.Direction.ASC)
                    .on("endDate", Sort.Direction.ASC)
                    .named("dateRange_idx"), "promotion_campaigns");
            log.info("PromotionCampaign indexes checked");

            IndexOperations analyticsIndexOps = mongoTemplate.indexOps("coupon_analytics");
            ensureIndex(analyticsIndexOps, new Index()
                    .on("couponId", Sort.Direction.ASC)
                    .unique()
                    .named("couponId_unique"), "coupon_analytics");
            ensureIndex(analyticsIndexOps, new Index()
                    .on("lastRedemption", Sort.Direction.DESC)
                    .named("lastRedemption_desc_idx"), "coupon_analytics");
            log.info("CouponAnalytics indexes checked");

            log.info("Coupon & Promotions index initialization complete");
        } catch (Exception e) {
            log.error("Error initializing coupon indexes", e);
            throw new RuntimeException("Failed to initialize coupon indexes", e);
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
