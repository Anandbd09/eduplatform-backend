package com.eduplatform.referral.service;

import com.eduplatform.referral.model.*;
import com.eduplatform.referral.repository.*;
import com.eduplatform.referral.dto.*;
import com.eduplatform.referral.exception.ReferralException;
import com.eduplatform.referral.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ReferralService {

    @Autowired
    private ReferralCodeRepository referralCodeRepository;

    @Autowired
    private ReferralClickRepository clickRepository;

    @Autowired
    private ReferralRewardRepository rewardRepository;

    @Autowired
    private ReferralPayoutRepository payoutRepository;

    @Autowired
    private ReferralAnalyticsRepository analyticsRepository;

    @Autowired
    private RewardService rewardService;

    @Autowired
    private PayoutService payoutService;

    @Autowired
    private TrackingService trackingService;

    /**
     * CREATE REFERRAL CODE FOR INSTRUCTOR
     */
    public ReferralCodeResponse createReferralCode(String instructorId, String instructorName,
                                                   String instructorEmail, String tenantId) {
        try {
            // Generate unique code
            String code = ReferralCodeGenerator.generateCode();

            // Check uniqueness
            Optional<ReferralCode> existing = referralCodeRepository
                    .findByReferralCodeAndTenantId(code, tenantId);
            if (existing.isPresent()) {
                return createReferralCode(instructorId, instructorName, instructorEmail, tenantId);
            }

            // Create referral code
            ReferralCode referralCode = ReferralCode.builder()
                    .id(UUID.randomUUID().toString())
                    .referralCode(code)
                    .instructorId(instructorId)
                    .instructorName(instructorName)
                    .instructorEmail(instructorEmail)
                    .status("ACTIVE")
                    .referralUrl("https://eduplatform.com?ref=" + code)
                    .totalClicks(0L)
                    .totalConversions(0L)
                    .totalRewardEarned(0.0)
                    .totalRewardPending(0.0)
                    .totalRewardPaid(0.0)
                    .createdAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();

            ReferralCode saved = referralCodeRepository.save(referralCode);

            // Create analytics record
            ReferralAnalytics analytics = ReferralAnalytics.builder()
                    .id(UUID.randomUUID().toString())
                    .referralCodeId(saved.getId())
                    .totalClicks(0L)
                    .totalConversions(0L)
                    .conversionRate(0.0)
                    .totalRevenueGenerated(0.0)
                    .totalRewardsPaid(0.0)
                    .uniqueVisitors(0L)
                    .uniqueCountries(0L)
                    .createdAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();
            analyticsRepository.save(analytics);

            log.info("Referral code created: code={}, instructor={}", code, instructorId);

            return convertToResponse(saved);

        } catch (Exception e) {
            log.error("Error creating referral code", e);
            throw new ReferralException("Failed to create referral code: " + e.getMessage());
        }
    }

    /**
     * TRACK REFERRAL CLICK (PUBLIC ENDPOINT)
     */
    public void trackReferralClick(String referralCode, String visitorEmail, String visitorIp,
                                   String userAgent, String deviceType, String country, String tenantId) {
        try {
            Optional<ReferralCode> code = referralCodeRepository
                    .findByReferralCodeAndTenantId(referralCode, tenantId);

            if (code.isEmpty()) {
                throw new ReferralException("Invalid referral code");
            }

            ReferralCode refCode = code.get();
            if (!refCode.isActive()) {
                throw new ReferralException("Referral code is not active");
            }

            // Create click record
            ReferralClick click = ReferralClick.builder()
                    .id(UUID.randomUUID().toString())
                    .referralCode(referralCode)
                    .instructorId(refCode.getInstructorId())
                    .visitorEmail(visitorEmail)
                    .visitorIp(visitorIp)
                    .visitorUserAgent(userAgent)
                    .visitorDeviceType(deviceType)
                    .visitorCountry(country)
                    .clickedAt(LocalDateTime.now())
                    .status("PENDING")
                    .expiresAt(LocalDateTime.now().plusDays(30))
                    .tenantId(tenantId)
                    .build();

            ReferralClick savedClick = clickRepository.save(click);

            // Update click counts
            refCode.setTotalClicks((refCode.getTotalClicks() != null ? refCode.getTotalClicks() : 0) + 1);
            referralCodeRepository.save(refCode);

            log.info("Referral click tracked: code={}, visitor={}", referralCode, visitorEmail);

        } catch (ReferralException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error tracking referral click", e);
            throw new ReferralException("Failed to track click");
        }
    }

    /**
     * RECORD REFERRAL CONVERSION (CALLED WHEN ORDER PLACED)
     */
    public void recordConversion(String referralCode, String orderId, String courseId,
                                 String courseName, Double coursePrice, String tenantId) {
        try {
            Optional<ReferralCode> code = referralCodeRepository
                    .findByReferralCodeAndTenantId(referralCode, tenantId);

            if (code.isEmpty()) {
                return; // No referral code, skip
            }

            ReferralCode refCode = code.get();

            // Calculate reward (20% of course price)
            Double rewardAmount = rewardService.calculateReward(coursePrice);

            // Create reward
            ReferralReward reward = ReferralReward.builder()
                    .id(UUID.randomUUID().toString())
                    .instructorId(refCode.getInstructorId())
                    .referralCode(referralCode)
                    .orderId(orderId)
                    .courseId(courseId)
                    .courseName(courseName)
                    .coursePrice(coursePrice)
                    .rewardAmount(rewardAmount)
                    .status("PENDING")
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusDays(30))
                    .purchasedAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();

            ReferralReward savedReward = rewardRepository.save(reward);

            // Update referral code stats
            refCode.setTotalConversions((refCode.getTotalConversions() != null ? refCode.getTotalConversions() : 0) + 1);
            refCode.setTotalRewardEarned((refCode.getTotalRewardEarned() != null ? refCode.getTotalRewardEarned() : 0) + rewardAmount);
            refCode.setTotalRewardPending((refCode.getTotalRewardPending() != null ? refCode.getTotalRewardPending() : 0) + rewardAmount);
            referralCodeRepository.save(refCode);

            // Update analytics
            updateAnalytics(refCode.getId(), tenantId);

            log.info("Referral conversion recorded: code={}, orderId={}, reward={}",
                    referralCode, orderId, rewardAmount);

        } catch (Exception e) {
            log.error("Error recording conversion", e);
        }
    }

    /**
     * GET REFERRAL CODE DETAILS
     */
    public ReferralCodeResponse getReferralCode(String referralCode, String tenantId) {
        try {
            Optional<ReferralCode> code = referralCodeRepository
                    .findByReferralCodeAndTenantId(referralCode, tenantId);

            if (code.isEmpty()) {
                throw new ReferralException("Referral code not found");
            }

            return convertToResponse(code.get());

        } catch (ReferralException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching referral code", e);
            throw new ReferralException("Failed to fetch referral code");
        }
    }

    /**
     * GET INSTRUCTOR'S REFERRAL CODES
     */
    public Page<ReferralCodeResponse> getInstructorCodes(String instructorId, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            Page<ReferralCode> codes = referralCodeRepository
                    .findByInstructorIdAndTenantId(instructorId, tenantId, pageable);

            return codes.map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error fetching instructor codes", e);
            throw new ReferralException("Failed to fetch referral codes");
        }
    }

    /**
     * GET PENDING REWARDS
     */
    public Page<ReferralRewardResponse> getPendingRewards(String instructorId, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            Page<ReferralReward> rewards = rewardRepository
                    .findByInstructorIdAndStatusAndTenantId(instructorId, "PENDING", tenantId, pageable);

            return rewards.map(this::convertRewardToResponse);

        } catch (Exception e) {
            log.error("Error fetching pending rewards", e);
            throw new ReferralException("Failed to fetch rewards");
        }
    }

    /**
     * REQUEST PAYOUT
     */
    public ReferralPayoutResponse requestPayout(String instructorId, String instructorName,
                                                String instructorEmail, String tenantId) {
        try {
            // Get all pending approved rewards
            Page<ReferralReward> rewards = rewardRepository
                    .findByInstructorIdAndStatusAndTenantId(instructorId, "APPROVED", tenantId,
                            PageRequest.of(0, 10000));

            if (rewards.isEmpty()) {
                throw new ReferralException("No approved rewards available for payout");
            }

            // Calculate total
            Double totalAmount = rewards.getContent().stream()
                    .mapToDouble(r -> r.getRewardAmount() != null ? r.getRewardAmount() : 0)
                    .sum();

            if (totalAmount <= 0) {
                throw new ReferralException("Payout amount must be greater than 0");
            }

            // Create payout request
            List<String> rewardIds = rewards.getContent().stream()
                    .map(ReferralReward::getId)
                    .collect(Collectors.toList());

            ReferralPayout payout = ReferralPayout.builder()
                    .id(UUID.randomUUID().toString())
                    .instructorId(instructorId)
                    .instructorName(instructorName)
                    .instructorEmail(instructorEmail)
                    .status("PENDING")
                    .rewardIds(rewardIds)
                    .rewardCount((long) rewardIds.size())
                    .totalAmount(totalAmount)
                    .platformFee(totalAmount * 0.05) // 5% fee
                    .netAmount(totalAmount * 0.95)
                    .requestedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();

            ReferralPayout savedPayout = payoutRepository.save(payout);

            // Mark rewards as in payout
            rewards.getContent().forEach(r -> {
                r.setStatus("IN_PAYOUT");
                rewardRepository.save(r);
            });

            log.info("Payout requested: instructor={}, amount={}, rewardCount={}",
                    instructorId, totalAmount, rewardIds.size());

            return convertPayoutToResponse(savedPayout);

        } catch (ReferralException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error requesting payout", e);
            throw new ReferralException("Failed to request payout");
        }
    }

    /**
     * GET INSTRUCTOR STATS
     */
    public ReferralStatsResponse getInstructorStats(String instructorId, String tenantId) {
        try {
            List<ReferralCode> codes = referralCodeRepository
                    .findByInstructorIdAndStatusAndTenantId(instructorId, "ACTIVE", tenantId);

            Long totalClicks = codes.stream()
                    .mapToLong(c -> c.getTotalClicks() != null ? c.getTotalClicks() : 0)
                    .sum();

            Long totalConversions = codes.stream()
                    .mapToLong(c -> c.getTotalConversions() != null ? c.getTotalConversions() : 0)
                    .sum();

            Double totalEarned = codes.stream()
                    .mapToDouble(c -> c.getTotalRewardEarned() != null ? c.getTotalRewardEarned() : 0)
                    .sum();

            Double totalPending = codes.stream()
                    .mapToDouble(c -> c.getTotalRewardPending() != null ? c.getTotalRewardPending() : 0)
                    .sum();

            Double totalPaid = codes.stream()
                    .mapToDouble(c -> c.getTotalRewardPaid() != null ? c.getTotalRewardPaid() : 0)
                    .sum();

            Double conversionRate = totalClicks > 0 ? ((double) totalConversions / totalClicks) * 100 : 0.0;

            return ReferralStatsResponse.builder()
                    .instructorId(instructorId)
                    .totalActiveCodes((long) codes.size())
                    .totalClicks(totalClicks)
                    .totalConversions(totalConversions)
                    .conversionRate(conversionRate)
                    .totalEarned(totalEarned)
                    .totalPending(totalPending)
                    .totalPaid(totalPaid)
                    .build();

        } catch (Exception e) {
            log.error("Error getting instructor stats", e);
            throw new ReferralException("Failed to get stats");
        }
    }

    /**
     * GET LEADERBOARD
     */
    public List<ReferralLeaderboardResponse> getLeaderboard(int limit, String tenantId) {
        try {
            List<ReferralCode> codes = referralCodeRepository
                    .findActiveCodes(tenantId);

            return codes.stream()
                    .sorted(Comparator.comparingDouble((ReferralCode c) ->
                            c.getTotalRewardEarned() != null ? c.getTotalRewardEarned() : 0).reversed())
                    .limit(limit)
                    .map(c -> ReferralLeaderboardResponse.builder()
                            .rank(0) // Will be set after sorting
                            .instructorId(c.getInstructorId())
                            .instructorName(c.getInstructorName())
                            .totalClicks(c.getTotalClicks())
                            .totalConversions(c.getTotalConversions())
                            .totalEarned(c.getTotalRewardEarned())
                            .conversionRate(c.getConversionRate())
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting leaderboard", e);
            throw new ReferralException("Failed to get leaderboard");
        }
    }

    /**
     * UPDATE ANALYTICS
     */
    private void updateAnalytics(String referralCodeId, String tenantId) {
        try {
            Optional<ReferralAnalytics> analytics = analyticsRepository
                    .findByReferralCodeIdAndTenantId(referralCodeId, tenantId);

            if (analytics.isEmpty()) {
                return;
            }

            ReferralAnalytics a = analytics.get();

            // Get related referral code
            Optional<ReferralCode> code = referralCodeRepository.findById(referralCodeId);
            if (code.isPresent()) {
                ReferralCode c = code.get();
                a.setTotalClicks(c.getTotalClicks());
                a.setTotalConversions(c.getTotalConversions());
                a.setConversionRate(c.getConversionRate());
                a.setTotalRevenueGenerated(c.getTotalRewardEarned());
                a.setLastUpdatedAt(LocalDateTime.now());
                analyticsRepository.save(a);
            }

        } catch (Exception e) {
            log.error("Error updating analytics", e);
        }
    }

    /**
     * CONVERT TO RESPONSE
     */
    private ReferralCodeResponse convertToResponse(ReferralCode code) {
        return ReferralCodeResponse.builder()
                .referralCode(code.getReferralCode())
                .referralUrl(code.getReferralUrl())
                .status(code.getStatus())
                .totalClicks(code.getTotalClicks())
                .totalConversions(code.getTotalConversions())
                .conversionRate(code.getConversionRate())
                .totalRewardEarned(code.getTotalRewardEarned())
                .totalRewardPending(code.getTotalRewardPending())
                .totalRewardPaid(code.getTotalRewardPaid())
                .createdAt(code.getCreatedAt())
                .build();
    }

    private ReferralRewardResponse convertRewardToResponse(ReferralReward reward) {
        return ReferralRewardResponse.builder()
                .rewardId(reward.getId())
                .referralCode(reward.getReferralCode())
                .courseName(reward.getCourseName())
                .coursePrice(reward.getCoursePrice())
                .rewardAmount(reward.getRewardAmount())
                .status(reward.getStatus())
                .createdAt(reward.getCreatedAt())
                .expiresAt(reward.getExpiresAt())
                .isExpired(reward.isExpired())
                .build();
    }

    private ReferralPayoutResponse convertPayoutToResponse(ReferralPayout payout) {
        return ReferralPayoutResponse.builder()
                .payoutId(payout.getId())
                .status(payout.getStatus())
                .rewardCount(payout.getRewardCount())
                .totalAmount(payout.getTotalAmount())
                .platformFee(payout.getPlatformFee())
                .netAmount(payout.getNetAmount())
                .requestedAt(payout.getRequestedAt())
                .approvedAt(payout.getApprovedAt())
                .paidAt(payout.getPaidAt())
                .build();
    }
}