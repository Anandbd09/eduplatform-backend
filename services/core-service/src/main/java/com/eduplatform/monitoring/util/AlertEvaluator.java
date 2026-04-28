// FILE 26: AlertEvaluator.java
package com.eduplatform.monitoring.util;

public class AlertEvaluator {

    /**
     * EVALUATE ALERT CONDITION
     */
    public static boolean evaluateCondition(Double currentValue, Double threshold, String operator) {
        if (currentValue == null || threshold == null) {
            return false;
        }

        switch (operator) {
            case "GREATER_THAN":
                return currentValue > threshold;
            case "LESS_THAN":
                return currentValue < threshold;
            case "EQUAL":
                return currentValue.equals(threshold);
            default:
                return false;
        }
    }

    /**
     * GET ALERT SEVERITY
     */
    public static String getSeverity(Double currentValue, Double warningThreshold, Double criticalThreshold) {
        if (currentValue >= criticalThreshold) {
            return "CRITICAL";
        } else if (currentValue >= warningThreshold) {
            return "WARNING";
        }
        return "INFO";
    }
}