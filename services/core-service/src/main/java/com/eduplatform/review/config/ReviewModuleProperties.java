package com.eduplatform.review.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Review module configuration properties bound from application.yml: review.module.*
 */
@Data
@Component
@ConfigurationProperties(prefix = "review.module")
public class ReviewModuleProperties {

    // How many days after creation can a review be edited? (default: 30)
    private int editWindowDays = 30;

    // Maximum length of review title (default: 100)
    private int maxTitleLength = 100;

    // Maximum length of review content (default: 5000)
    private int maxContentLength = 5000;

    // Minimum length of review title (default: 5)
    private int minTitleLength = 5;

    // Minimum length of review content (default: 20)
    private int minContentLength = 20;

    // Default pagination page size (default: 10)
    private int defaultPageSize = 10;

    // Maximum pagination page size allowed (default: 100)
    private int maxPageSize = 100;

    // Decimal places for rating calculation (default: 1 = one decimal place)
    private int ratingPrecision = 1;

    // Auto-approve reviews when created (default: true)
    private boolean autoApproveReviews = true;

    // Enable helpful/unhelpful voting feature (default: true)
    private boolean enableHelpfulVoting = true;

    // Minimum helpful count to show "most helpful" badge (default: 5)
    private int minHelpfulCountForBadge = 5;

    // Allow anonymous reviews (default: false)
    private boolean allowAnonymousReviews = false;
}
