// FILE 24: AchievementChecker.java
package com.eduplatform.gamification.util;

public class AchievementChecker {

    /**
     * CHECK IF ACHIEVEMENT UNLOCKED
     */
    public static boolean isAchievementUnlocked(String code, Integer value) {
        switch (code) {
            case "FIRST_LESSON":
                return value >= 1;
            case "SEVEN_DAY_STREAK":
                return value >= 7;
            case "THIRTY_DAY_STREAK":
                return value >= 30;
            case "PERFECT_SCORE":
                return value == 100;
            default:
                return false;
        }
    }
}