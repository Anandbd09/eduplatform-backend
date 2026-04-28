package com.eduplatform.coupon.service;

import com.eduplatform.coupon.model.CouponAnalytics;
import com.eduplatform.coupon.repository.CouponAnalyticsRepository;
import com.eduplatform.coupon.repository.CouponRedemptionRepository;
import com.eduplatform.coupon.dto.CouponAnalyticsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    @Autowired
    private CouponAnalyticsRepository analyticsRepository;

    @Autowired
    private CouponRedemptionRepository redemptionRepository;

    /**
     * GET DETAILED ANALYTICS FOR COUPON
     */
    public CouponAnalyticsResponse getDetailedAnalytics(String couponId, String tenantId) {
        try {
            var analytics = analyticsRepository.findByCouponIdAndTenantId(couponId, tenantId)
                    .orElse(null);

            if (analytics == null) {
                return CouponAnalyticsResponse.builder()
                        .couponId(couponId)
                        .totalRedemptions(0L)
                        .totalDiscountGiven(0.0)
                        .build();
            }

            // Calculate conversion rate
            Double conversionRate = 0.0;
            if (analytics.getImpressions() != null && analytics.getImpressions() > 0) {
                conversionRate = (analytics.getTotalRedemptions() * 100.0) / analytics.getImpressions();
            }

            return CouponAnalyticsResponse.builder()
                    .couponId(analytics.getCouponId())
                    .totalRedemptions(analytics.getTotalRedemptions())
                    .totalDiscountGiven(analytics.getTotalDiscountGiven())
                    .totalRevenue(analytics.getTotalRevenue())
                    .conversionRate(conversionRate)
                    .uniqueUsers(analytics.getUniqueUsers())
                    .avgDiscountPerRedemption(
                            analytics.getTotalRedemptions() > 0 ?
                                    analytics.getTotalDiscountGiven() / analytics.getTotalRedemptions() : 0.0
                    )
                    .lastRedemption(analytics.getLastRedemption())
                    .build();

        } catch (Exception e) {
            log.error("Error getting analytics", e);
            return CouponAnalyticsResponse.builder().build();
        }
    }

    /**
     * UPDATE ANALYTICS METRICS
     */
    public void updateMetrics(String couponId, String tenantId) {
        try {
            var analytics = analyticsRepository.findByCouponIdAndTenantId(couponId, tenantId)
                    .orElse(null);

            if (analytics != null) {
                // Get recent successful redemptions (last 30 days)
                LocalDateTime thirtyDaysAgo = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
                var recentRedemptions = redemptionRepository
                        .findRecentSuccessfulRedemptions(thirtyDaysAgo, tenantId);

                if (!recentRedemptions.isEmpty()) {
                    analytics.setLastRedemption(LocalDateTime.now());
                    analyticsRepository.save(analytics);
                }
            }
        } catch (Exception e) {
            log.error("Error updating metrics", e);
        }
    }
}