package com.eduplatform.recommendation.service;

import com.eduplatform.recommendation.model.*;
import com.eduplatform.recommendation.repository.*;
import com.eduplatform.recommendation.dto.*;
import com.eduplatform.recommendation.exception.RecommendationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class RecommendationService {

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Autowired
    private CourseVectorRepository courseVectorRepository;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private CourseSimilarityRepository courseSimilarityRepository;

    @Autowired
    private CollaborativeFilteringService collaborativeFilteringService;

    @Autowired
    private ContentBasedService contentBasedService;

    @Autowired
    private SimilarityService similarityService;

    private static final String CACHE_KEY_PREFIX = "recommendations:";

    private final Map<String, CachedRecommendation> recommendationCache = new ConcurrentHashMap<>();

    @Value("${recommendation.cache.ttl-hours:24}")
    private long cacheTtlHours;

    @Value("${recommendation.cache.top-courses-limit:100}")
    private int topCoursesLimit;

    @Value("${recommendation.algorithm.collaborative-weight:0.5}")
    private double collaborativeWeight;

    @Value("${recommendation.algorithm.content-weight:0.5}")
    private double contentWeight;

    @Value("${recommendation.algorithm.similarity-threshold:0.7}")
    private double similarityThreshold;

    /**
     * Get personalized recommendations for user
     */
    public Page<RecommendationResponse> getPersonalizedRecommendations(
            String userId, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            String cacheKey = buildCacheKey(userId, tenantId);

            CachedRecommendation cachedRecommendation = recommendationCache.get(cacheKey);
            if (cachedRecommendation != null) {
                if (!cachedRecommendation.isExpired() && !cachedRecommendation.courseIds().isEmpty()) {
                    log.debug("Cache hit for user: {} tenant: {}", userId, tenantId);
                    return fetchRecommendationPage(userId, page, size, tenantId);
                }
                recommendationCache.remove(cacheKey);
            }

            // Calculate hybrid recommendations
            List<RecommendationRecord> recommendations = calculateHybridRecommendations(userId, tenantId);

            cacheRecommendations(cacheKey, recommendations);
            return fetchRecommendationPage(userId, page, size, tenantId);

        } catch (Exception e) {
            log.error("Error getting personalized recommendations", e);
            throw new RecommendationException("Failed to get recommendations");
        }
    }

    /**
     * Calculate hybrid recommendations (50% collaborative + 50% content)
     */
    private List<RecommendationRecord> calculateHybridRecommendations(String userId, String tenantId) {
        List<RecommendationRecord> recommendations = new ArrayList<>();

        try {
            // Get collaborative scores
            Map<String, Double> collaborativeScores = collaborativeFilteringService
                    .getCollaborativeScores(userId, tenantId);

            // Get content-based scores
            Map<String, Double> contentScores = contentBasedService
                    .getContentBasedScores(userId, tenantId);

            // Merge and calculate hybrid scores
            Set<String> allCourses = new HashSet<>();
            allCourses.addAll(collaborativeScores.keySet());
            allCourses.addAll(contentScores.keySet());

            for (String courseId : allCourses) {
                Double collabScore = collaborativeScores.getOrDefault(courseId, 0.0);
                Double contentScore = contentScores.getOrDefault(courseId, 0.0);

                Double hybridScore = calculateHybridScore(collabScore, contentScore);

                RecommendationRecord record = RecommendationRecord.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(userId)
                        .courseId(courseId)
                        .collaborativeScore(collabScore)
                        .contentScore(contentScore)
                        .score(hybridScore)
                        .reason("HYBRID")
                        .createdAt(LocalDateTime.now())
                        .tenantId(tenantId)
                        .build();

                recommendations.add(record);
            }

            // Sort by score descending
            recommendations.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

            // Save to database
            recommendationRepository.saveAll(recommendations);

            log.info("Calculated {} hybrid recommendations for user: {}", recommendations.size(), userId);
            return recommendations;

        } catch (Exception e) {
            log.error("Error calculating hybrid recommendations", e);
            throw new RecommendationException("Failed to calculate recommendations");
        }
    }

    /**
     * Track user interaction with course
     */
    public void trackInteraction(String userId, String courseId, String interactionType,
                                 Double rating, String tenantId) {
        try {
            // Check for existing preference
            Optional<UserPreference> existing = userPreferenceRepository
                    .findByUserIdAndCourseIdAndTenantId(userId, courseId, tenantId);

            UserPreference preference;
            if (existing.isPresent()) {
                preference = existing.get();
                preference.setInteractionType(interactionType);
                preference.setInteractionDate(LocalDateTime.now());
            } else {
                preference = UserPreference.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(userId)
                        .courseId(courseId)
                        .interactionType(interactionType)
                        .interactionDate(LocalDateTime.now())
                        .tenantId(tenantId)
                        .build();
            }

            // Set weight based on interaction type
            switch (interactionType) {
                case "VIEW" -> preference.setWeight(1);
                case "CLICK" -> preference.setWeight(2);
                case "ENROLL" -> preference.setWeight(3);
                case "RATE" -> preference.setWeight(4);
                case "COMPLETE" -> preference.setWeight(5);
            }

            // Set rating if provided
            if (rating != null) {
                preference.setRating(rating);
            }

            // Calculate engagement score
            preference.calculateEngagementScore();

            // Save
            userPreferenceRepository.save(preference);

            // Invalidate cache
            invalidateCache(userId, tenantId);

            log.info("Tracked interaction: userId={}, courseId={}, type={}", userId, courseId, interactionType);

        } catch (Exception e) {
            log.error("Error tracking interaction", e);
            throw new RecommendationException("Failed to track interaction");
        }
    }

    /**
     * Mark recommendation as clicked
     */
    public void markRecommendationClicked(String recommendationId, String tenantId) {
        try {
            Optional<RecommendationRecord> record = recommendationRepository.findById(recommendationId);
            if (record.isPresent()) {
                RecommendationRecord rec = record.get();
                rec.markClicked();
                recommendationRepository.save(rec);
                log.info("Recommendation marked as clicked: {}", recommendationId);
            }
        } catch (Exception e) {
            log.error("Error marking recommendation as clicked", e);
            throw new RecommendationException("Failed to mark as clicked");
        }
    }

    /**
     * Get trending courses
     */
    public List<TrendingResponse> getTrendingCourses(int limit, String tenantId) {
        try {
            // Get courses with highest ratings and recent interactions
            List<CourseVector> courses = courseVectorRepository.findByRatingGreaterThanAndTenantId(4.0, tenantId);

            return courses.stream()
                    .limit(limit)
                    .map(course -> TrendingResponse.builder()
                            .courseId(course.getCourseId())
                            .category(course.getCategory())
                            .rating(course.getRating())
                            .duration(course.getDuration())
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting trending courses", e);
            throw new RecommendationException("Failed to get trending courses");
        }
    }

    /**
     * Get similar courses
     */
    public List<SimilarCourseResponse> getSimilarCourses(String courseId, int limit, String tenantId) {
        try {
            List<CourseSimilarity> similarities = courseSimilarityRepository
                    .findBySourceCourseIdAndSimilarityScoreGreaterThanAndTenantId(courseId, similarityThreshold, tenantId);

            return similarities.stream()
                    .limit(limit)
                    .map(sim -> SimilarCourseResponse.builder()
                            .courseId(sim.getTargetCourseId())
                            .similarityScore(sim.getSimilarityScore())
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting similar courses", e);
            throw new RecommendationException("Failed to get similar courses");
        }
    }

    /**
     * Get recommendations by reason
     */
    public Page<RecommendationResponse> getRecommendationsByReason(String userId, String reason,
                                                                   int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("score").descending());

            return recommendationRepository.findByUserIdAndReasonAndTenantId(userId, reason, tenantId, pageable)
                    .map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error getting recommendations by reason", e);
            throw new RecommendationException("Failed to get recommendations");
        }
    }

    /**
     * Get user analytics
     */
    public RecommendationAnalytics getUserAnalytics(String userId, String tenantId) {
        try {
            List<UserPreference> preferences = userPreferenceRepository.findByUserIdAndTenantId(userId, tenantId);

            Long viewCount = preferences.stream().filter(p -> "VIEW".equals(p.getInteractionType())).count();
            Long enrollCount = preferences.stream().filter(p -> "ENROLL".equals(p.getInteractionType())).count();
            Long completeCount = preferences.stream().filter(p -> "COMPLETE".equals(p.getInteractionType())).count();
            Long rateCount = preferences.stream().filter(p -> "RATE".equals(p.getInteractionType())).count();

            Double avgEngagement = preferences.stream()
                    .mapToDouble(p -> p.getEngagementScore() != null ? p.getEngagementScore() : 0)
                    .average()
                    .orElse(0.0);

            return RecommendationAnalytics.builder()
                    .userId(userId)
                    .totalInteractions(preferences.size())
                    .viewCount(viewCount.intValue())
                    .enrollCount(enrollCount.intValue())
                    .completeCount(completeCount.intValue())
                    .rateCount(rateCount.intValue())
                    .avgEngagementScore(avgEngagement)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error getting user analytics", e);
            throw new RecommendationException("Failed to get analytics");
        }
    }

    /**
     * Get clicked recommendations
     */
    public Page<RecommendationResponse> getClickedRecommendations(String userId, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("clickedAt").descending());

            return recommendationRepository.findByUserIdAndClickedTrueAndTenantId(userId, tenantId, pageable)
                    .map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error getting clicked recommendations", e);
            throw new RecommendationException("Failed to get clicked recommendations");
        }
    }

    /**
     * Recalculate recommendations for user
     */
    public void recalculateForUser(String userId, String tenantId) {
        try {
            log.info("Recalculating recommendations for user: {}", userId);
            calculateHybridRecommendations(userId, tenantId);
            invalidateCache(userId, tenantId);
        } catch (Exception e) {
            log.error("Error recalculating recommendations", e);
            throw new RecommendationException("Failed to recalculate recommendations");
        }
    }

    /**
     * Invalidate cache for user
     */
    private void invalidateCache(String userId, String tenantId) {
        String cacheKey = buildCacheKey(userId, tenantId);
        recommendationCache.remove(cacheKey);
        log.debug("Invalidated cache for user: {} tenant: {}", userId, tenantId);
    }

    /**
     * Convert to response DTO
     */
    private RecommendationResponse convertToResponse(RecommendationRecord record) {
        return RecommendationResponse.builder()
                .id(record.getId())
                .courseId(record.getCourseId())
                .score(record.getScore())
                .reason(record.getReason())
                .clicked(record.getClicked())
                .createdAt(record.getCreatedAt())
                .build();
    }

    private Page<RecommendationResponse> fetchRecommendationPage(String userId, int page, int size, String tenantId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("score").descending());
        return recommendationRepository.findByUserIdAndTenantId(userId, tenantId, pageable)
                .map(this::convertToResponse);
    }

    private void cacheRecommendations(String cacheKey, List<RecommendationRecord> recommendations) {
        if (recommendations.isEmpty()) {
            recommendationCache.remove(cacheKey);
            return;
        }

        List<String> courseIds = recommendations.stream()
                .limit(Math.max(topCoursesLimit, 1))
                .map(RecommendationRecord::getCourseId)
                .collect(Collectors.toList());

        recommendationCache.put(
                cacheKey,
                new CachedRecommendation(courseIds, LocalDateTime.now().plusHours(Math.max(cacheTtlHours, 1)))
        );
        log.debug("Cached {} recommendations for key: {}", courseIds.size(), cacheKey);
    }

    private Double calculateHybridScore(Double collaborativeScore, Double contentScore) {
        double totalWeight = collaborativeWeight + contentWeight;
        double resolvedCollaborativeWeight = totalWeight > 0 ? collaborativeWeight / totalWeight : 0.5;
        double resolvedContentWeight = totalWeight > 0 ? contentWeight / totalWeight : 0.5;

        return (collaborativeScore * resolvedCollaborativeWeight)
                + (contentScore * resolvedContentWeight);
    }

    private String buildCacheKey(String userId, String tenantId) {
        return CACHE_KEY_PREFIX + tenantId + ":" + userId;
    }

    private record CachedRecommendation(List<String> courseIds, LocalDateTime expiresAt) {
        private boolean isExpired() {
            return expiresAt == null || LocalDateTime.now().isAfter(expiresAt);
        }
    }
}
