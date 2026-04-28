package com.eduplatform.gamification.service;

import com.eduplatform.gamification.model.*;
import com.eduplatform.gamification.repository.*;
import com.eduplatform.gamification.dto.*;
import com.eduplatform.gamification.exception.GamificationException;
import com.eduplatform.gamification.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@Transactional
public class GamificationService {

    @Autowired
    private DailyStreakRepository streakRepository;

    @Autowired
    private UserPointsRepository pointsRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private StreakService streakService;

    @Autowired
    private LeaderboardService leaderboardService;

    /**
     * RECORD LESSON COMPLETION & UPDATE STREAK
     */
    public StreakResponse recordLessonCompletion(String courseId, String userId, String tenantId) {
        try {
            String streakKey = userId + ":" + courseId;
            Optional<DailyStreak> existingStreak = streakRepository
                    .findByUserIdAndCourseIdAndTenantId(userId, courseId, tenantId);

            DailyStreak streak = existingStreak.orElseGet(() -> DailyStreak.builder()
                    .id(UUID.randomUUID().toString())
                    .streakKey(streakKey)
                    .userId(userId)
                    .courseId(courseId)
                    .currentStreak(0)
                    .longestStreak(0)
                    .totalLessonsCompleted(0)
                    .totalMinutesSpent(0)
                    .isActive(true)
                    .streakStartDate(LocalDate.now())
                    .createdAt(LocalDate.now())
                    .tenantId(tenantId)
                    .build());

            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);

            // Check if streak continues
            if (streak.getLastActivityDate() == null || streak.getLastActivityDate().isBefore(yesterday)) {
                // Streak broken, start new one
                streak.setCurrentStreak(1);
                streak.setStreakStartDate(today);
                streak.setIsActive(true);
            } else if (!streak.getLastActivityDate().equals(today)) {
                // Activity today, continue streak
                streak.setCurrentStreak(streak.getCurrentStreak() + 1);
            }

            // Update longest streak
            if (streak.getCurrentStreak() > streak.getLongestStreak()) {
                streak.setLongestStreak(streak.getCurrentStreak());
                streak.setLongestStreakStartDate(streak.getStreakStartDate());
                streak.setLongestStreakEndDate(today);
            }

            streak.setLastActivityDate(today);
            streak.setTotalLessonsCompleted(streak.getTotalLessonsCompleted() + 1);
            streak.setUpdatedAt(LocalDate.now());

            streakRepository.save(streak);

            // Award points for lesson completion
            awardPoints(userId, 50, "Lesson completed", tenantId);

            // Check for streak bonuses
            if (streak.getCurrentStreak() == 7) {
                awardPoints(userId, 250, "7-day streak bonus", tenantId);
                unlockAchievement(userId, "SEVEN_DAY_STREAK", tenantId);
            } else if (streak.getCurrentStreak() == 30) {
                awardPoints(userId, 1000, "30-day streak bonus", tenantId);
                unlockAchievement(userId, "THIRTY_DAY_STREAK", tenantId);
            }

            log.info("Lesson completion recorded: userId={}, courseId={}, streak={}",
                    userId, courseId, streak.getCurrentStreak());

            return StreakResponse.builder()
                    .currentStreak(streak.getCurrentStreak())
                    .longestStreak(streak.getLongestStreak())
                    .totalLessonsCompleted(streak.getTotalLessonsCompleted())
                    .isStreakAlive(true)
                    .build();

        } catch (Exception e) {
            log.error("Error recording lesson completion", e);
            throw new GamificationException("Failed to record lesson completion");
        }
    }

    /**
     * GET USER STREAK
     */
    public StreakResponse getUserStreak(String courseId, String userId, String tenantId) {
        try {
            Optional<DailyStreak> streak = streakRepository
                    .findByUserIdAndCourseIdAndTenantId(userId, courseId, tenantId);

            if (streak.isEmpty()) {
                return StreakResponse.builder()
                        .currentStreak(0)
                        .longestStreak(0)
                        .totalLessonsCompleted(0)
                        .isStreakAlive(false)
                        .build();
            }

            DailyStreak s = streak.get();
            return StreakResponse.builder()
                    .currentStreak(s.getCurrentStreak())
                    .longestStreak(s.getLongestStreak())
                    .totalLessonsCompleted(s.getTotalLessonsCompleted())
                    .isStreakAlive(s.isStreakAlive())
                    .build();

        } catch (Exception e) {
            log.error("Error getting user streak", e);
            throw new GamificationException("Failed to get user streak");
        }
    }

    /**
     * GET USER POINTS & LEVEL
     */
    public PointsResponse getUserPoints(String userId, String tenantId) {
        try {
            Optional<UserPoints> pointsOpt = pointsRepository.findByUserIdAndTenantId(userId, tenantId);

            if (pointsOpt.isEmpty()) {
                return PointsResponse.builder()
                        .totalPoints(0L)
                        .currentLevel(1)
                        .pointsInCurrentLevel(0L)
                        .userRank("BRONZE")
                        .globalRank(0)
                        .build();
            }

            UserPoints points = pointsOpt.get();
            return PointsResponse.builder()
                    .totalPoints(points.getTotalPoints())
                    .currentLevel(points.getCurrentLevel())
                    .pointsInCurrentLevel(points.getPointsInCurrentLevel())
                    .userRank(points.getUserRank())
                    .globalRank(points.getGlobalRank())
                    .pointsToNextLevel(PointsCalculator.pointsToNextLevel(points.getPointsInCurrentLevel()))
                    .build();

        } catch (Exception e) {
            log.error("Error getting user points", e);
            throw new GamificationException("Failed to get user points");
        }
    }

    /**
     * GET USER ACHIEVEMENTS
     */
    public Page<AchievementResponse> getUserAchievements(String userId, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size);

            Page<UserAchievement> achievements = userAchievementRepository
                    .findByUserIdAndIsUnlockedTrueAndTenantId(userId, true, tenantId, pageable);

            return achievements.map(ua -> AchievementResponse.builder()
                    .id(ua.getAchievementId())
                    .isUnlocked(ua.getIsUnlocked())
                    .unlockedAt(ua.getUnlockedAt())
                    .progress(ua.getProgress())
                    .build());

        } catch (Exception e) {
            log.error("Error getting user achievements", e);
            throw new GamificationException("Failed to get user achievements");
        }
    }

    /**
     * GET LEADERBOARD
     */
    public Page<LeaderboardEntryResponse> getLeaderboard(int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("totalPoints").descending());

            Page<UserPoints> leaders = pointsRepository.findByTenantIdOrderByTotalPointsDesc(tenantId, pageable);
            List<LeaderboardEntryResponse> leaderboardEntries = new ArrayList<>();
            int rank = (int) pageable.getOffset() + 1;

            for (UserPoints leader : leaders.getContent()) {
                leaderboardEntries.add(LeaderboardEntryResponse.builder()
                        .rank(rank++)
                        .userId(leader.getUserId())
                        .totalPoints(leader.getTotalPoints())
                        .currentLevel(leader.getCurrentLevel())
                        .userRank(leader.getUserRank())
                        .build());
            }

            return new PageImpl<>(leaderboardEntries, pageable, leaders.getTotalElements());

        } catch (Exception e) {
            log.error("Error getting leaderboard", e);
            throw new GamificationException("Failed to get leaderboard");
        }
    }

    /**
     * HELPER: Award points
     */
    private void awardPoints(String userId, Integer pointsToAdd, String reason, String tenantId) {
        try {
            Optional<UserPoints> pointsOpt = pointsRepository.findByUserIdAndTenantId(userId, tenantId);

            UserPoints points = pointsOpt.orElseGet(() -> UserPoints.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .totalPoints(0L)
                    .currentLevel(1)
                    .pointsInCurrentLevel(0L)
                    .userRank("BRONZE")
                    .createdAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build());

            points.setTotalPoints(points.getTotalPoints() + pointsToAdd);
            points.setPointsInCurrentLevel(points.getPointsInCurrentLevel() + pointsToAdd);
            points.setLastPointsEarned(LocalDateTime.now());

            // Check for level up
            if (points.getPointsInCurrentLevel() >= 500) {
                points.setCurrentLevel(points.getCurrentLevel() + 1);
                points.setPointsInCurrentLevel(points.getPointsInCurrentLevel() - 500);
                points.setUserRank(PointsCalculator.getRankForLevel(points.getCurrentLevel()));
                unlockAchievement(userId, "LEVEL_UP", tenantId);
            }

            pointsRepository.save(points);
            log.debug("Points awarded: userId={}, points={}, reason={}", userId, pointsToAdd, reason);

        } catch (Exception e) {
            log.warn("Error awarding points", e);
        }
    }

    /**
     * HELPER: Unlock achievement
     */
    private void unlockAchievement(String userId, String achievementCode, String tenantId) {
        try {
            Optional<Achievement> achievementOpt = achievementRepository
                    .findByAchievementCodeAndTenantId(achievementCode, tenantId);

            if (achievementOpt.isEmpty()) return;

            Achievement achievement = achievementOpt.get();

            UserAchievement userAchievement = UserAchievement.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .achievementId(achievement.getId())
                    .isUnlocked(true)
                    .unlockedAt(LocalDateTime.now())
                    .progress(100)
                    .tenantId(tenantId)
                    .build();

            userAchievementRepository.save(userAchievement);
            awardPoints(userId, achievement.getPointsReward(), "Achievement: " + achievement.getTitle(), tenantId);

            log.info("Achievement unlocked: userId={}, achievement={}", userId, achievementCode);

        } catch (Exception e) {
            log.warn("Error unlocking achievement", e);
        }
    }
}
