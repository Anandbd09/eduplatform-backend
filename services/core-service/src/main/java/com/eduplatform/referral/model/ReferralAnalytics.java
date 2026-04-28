package com.eduplatform.referral.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "referral_analytics")
public class ReferralAnalytics {

    @Id
    private String id;

    @Indexed(unique = true)
    private String referralCodeId;

    private Long totalClicks;

    private Long totalConversions;

    private Double conversionRate; // percentage

    private Double totalRevenueGenerated;

    private Double totalRewardsPaid;

    private Long uniqueVisitors;

    private Long uniqueCountries;

    private String topCountry;

    private String topDeviceType;

    private Long clicksLast7Days;

    private Long clicksLast30Days;

    private Double avgRewardPerClick;

    @Indexed
    private LocalDateTime lastUpdatedAt;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;
}