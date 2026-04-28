// FILE 23: PointsCalculator.java
package com.eduplatform.gamification.util;

public class PointsCalculator {

    private static final Integer POINTS_PER_LEVEL = 500;

    /**
     * GET RANK FOR LEVEL
     */
    public static String getRankForLevel(Integer level) {
        if (level < 20) return "BRONZE";
        if (level < 50) return "SILVER";
        if (level < 75) return "GOLD";
        if (level < 95) return "PLATINUM";
        return "DIAMOND";
    }

    /**
     * GET POINTS TO NEXT LEVEL
     */
    public static Long pointsToNextLevel(Long currentPoints) {
        return Math.max(0, POINTS_PER_LEVEL - currentPoints);
    }
}