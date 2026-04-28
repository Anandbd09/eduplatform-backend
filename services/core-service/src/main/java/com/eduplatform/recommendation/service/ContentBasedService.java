package com.eduplatform.recommendation.service;

import com.eduplatform.recommendation.model.*;
import com.eduplatform.recommendation.repository.*;
import com.eduplatform.recommendation.util.SimilarityCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ContentBasedService {

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Autowired
    private CourseVectorRepository courseVectorRepository;

    /**
     * Get content-based filtering scores
     * Build user profile from preferences and find similar courses
     */
    public Map<String, Double> getContentBasedScores(String userId, String tenantId) {
        Map<String, Double> scores = new HashMap<>();

        try {
            // Get user's preferences
            List<UserPreference> userPrefs = userPreferenceRepository.findByUserIdAndTenantId(userId, tenantId);

            if (userPrefs.isEmpty()) {
                return scores;
            }

            // Build weighted user profile from preferences
            Map<String, Double> userProfile = buildUserProfile(userPrefs, tenantId);

            // Get all courses
            List<CourseVector> allCourses = courseVectorRepository.findAll();

            // Calculate similarity with each course
            Set<String> userCourseIds = userPrefs.stream()
                    .map(UserPreference::getCourseId)
                    .collect(Collectors.toSet());

            for (CourseVector course : allCourses) {
                // Skip courses user already interacted with
                if (userCourseIds.contains(course.getCourseId())) {
                    continue;
                }

                if (course.getFeatureVector() != null) {
                    Double similarity = SimilarityCalculator.featureSimilarity(
                            userProfile, course.getFeatureVector());

                    if (similarity > 0.3) { // Only include courses with >30% similarity
                        scores.put(course.getCourseId(), similarity * 100);
                    }
                }
            }

            // Normalize scores to 0-100
            normalizeScores(scores);

            log.debug("Calculated content-based scores for {} courses", scores.size());
            return scores;

        } catch (Exception e) {
            log.error("Error calculating content-based scores", e);
            return scores;
        }
    }

    /**
     * Build weighted user profile from preferences
     */
    private Map<String, Double> buildUserProfile(List<UserPreference> userPrefs, String tenantId) {
        Map<String, Double> userProfile = new HashMap<>();

        for (UserPreference pref : userPrefs) {
            CourseVector course = courseVectorRepository.findByCourseIdAndTenantId(
                    pref.getCourseId(), tenantId).orElse(null);

            if (course != null && course.getFeatureVector() != null) {
                Double weight = (pref.getEngagementScore() != null ? pref.getEngagementScore() : 50) / 100.0;

                for (Map.Entry<String, Double> feature : course.getFeatureVector().entrySet()) {
                    userProfile.put(feature.getKey(),
                            userProfile.getOrDefault(feature.getKey(), 0.0) + (feature.getValue() * weight));
                }
            }
        }

        return userProfile;
    }

    /**
     * Normalize scores to 0-100 range
     */
    private void normalizeScores(Map<String, Double> scores) {
        if (scores.isEmpty()) return;

        Double maxScore = scores.values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(1.0);

        scores.replaceAll((k, v) -> (v / maxScore) * 100);
    }
}