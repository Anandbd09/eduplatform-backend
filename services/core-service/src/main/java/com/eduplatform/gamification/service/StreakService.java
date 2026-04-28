package com.eduplatform.gamification.service;

import com.eduplatform.gamification.model.DailyStreak;
import com.eduplatform.gamification.repository.DailyStreakRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;

@Slf4j
@Service
public class StreakService {

    @Autowired
    private DailyStreakRepository streakRepository;

    /**
     * CHECK IF USER HAS ACTIVE STREAK
     */
    public boolean hasActiveStreak(String userId, String courseId, String tenantId) {
        return streakRepository
                .findByUserIdAndCourseIdAndTenantId(userId, courseId, tenantId)
                .map(DailyStreak::isStreakAlive)
                .orElse(false);
    }
}