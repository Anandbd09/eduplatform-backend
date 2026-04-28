package com.eduplatform.reporting.util;

public class PriorityCalculator {

    /**
     * Calculate dispute priority (1-10)
     * Higher = more urgent
     */
    public static int calculatePriority(String severity, String category, int evidenceCount) {
        int priority = 5; // Base priority

        // Severity impact (max +3)
        switch (severity) {
            case "CRITICAL" -> priority += 3;
            case "HIGH" -> priority += 2;
            case "MEDIUM" -> priority += 1;
            case "LOW" -> priority += 0;
        }

        // Category impact (max +2)
        switch (category) {
            case "FRAUD" -> priority += 2;
            case "PLAGIARISM" -> priority += 2;
            case "INAPPROPRIATE" -> priority += 1;
            case "COPYRIGHT" -> priority += 1;
            case "SPAM" -> priority += 0;
        }

        // Evidence impact (max +1)
        if (evidenceCount >= 3) {
            priority += 1;
        }

        // Clamp to 1-10
        return Math.max(1, Math.min(10, priority));
    }
}