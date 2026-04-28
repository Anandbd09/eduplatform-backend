package com.eduplatform.recommendation.service;

import com.eduplatform.recommendation.model.*;
import com.eduplatform.recommendation.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class CollaborativeFilteringService {

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    /**
     * Get collaborative filtering scores
     * Find similar users and recommend courses they liked
     */
    public Map<String, Double> getCollaborativeScores(String userId, String tenantId) {
        Map<String, Double> scores = new HashMap<>();

        try {
            // Get user's preferences
            List<UserPreference> userPrefs = userPreferenceRepository.findByUserIdAndTenantId(userId, tenantId);

            if (userPrefs.isEmpty()) {
                return scores;
            }

            // Find similar users (users who liked same courses)
            Set<String> userCourses = userPrefs.stream()
                    .map(UserPreference::getCourseId)
                    .collect(Collectors.toSet());

            // Get all users who liked these courses
            Map<String, Double> similarUsers = new HashMap<>();
            for (String courseId : userCourses) {
                List<UserPreference> otherPrefs = userPreferenceRepository.findByUserIdAndTenantId(userId, tenantId);

                for (UserPreference pref : otherPrefs) {
                    if (!pref.getUserId().equals(userId)) {
                        similarUsers.put(pref.getUserId(),
                                similarUsers.getOrDefault(pref.getUserId(), 0.0) + 1.0);
                    }
                }
            }

            // Get courses liked by similar users
            for (String similarUserId : similarUsers.keySet()) {
                List<UserPreference> similarUserPrefs = userPreferenceRepository
                        .findByUserIdAndTenantId(similarUserId, tenantId);

                for (UserPreference pref : similarUserPrefs) {
                    if (!userCourses.contains(pref.getCourseId())) {
                        Double score = scores.getOrDefault(pref.getCourseId(), 0.0);
                        score += (pref.getEngagementScore() != null ? pref.getEngagementScore() : 0) * 0.5;
                        scores.put(pref.getCourseId(), score);
                    }
                }
            }

            // Normalize scores to 0-100
            normalizeScores(scores);

            log.debug("Calculated collaborative scores for {} courses", scores.size());
            return scores;

        } catch (Exception e) {
            log.error("Error calculating collaborative scores", e);
            return scores;
        }
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