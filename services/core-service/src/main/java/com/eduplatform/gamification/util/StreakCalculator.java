// FILE 22: StreakCalculator.java
package com.eduplatform.gamification.util;

import java.time.LocalDate;

public class StreakCalculator {

    /**
     * CALCULATE STREAK BONUS
     */
    public static Integer calculateStreakBonus(Integer days) {
        if (days < 7) return 0;
        if (days < 30) return 250;
        if (days < 100) return 1000;
        return 5000;
    }
}