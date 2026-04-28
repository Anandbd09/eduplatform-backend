// FILE 27: ValidationUtil.java
package com.eduplatform.coupon.util;

import java.time.LocalDateTime;

public class ValidationUtil {

    /**
     * Check if date range is valid
     */
    public static boolean isValidDateRange(LocalDateTime from, LocalDateTime until) {
        if (from == null || until == null) {
            return false;
        }
        return from.isBefore(until);
    }

    /**
     * Check if amount is valid
     */
    public static boolean isValidAmount(Double amount) {
        return amount != null && amount > 0;
    }

    /**
     * Validate coupon code format
     */
    public static boolean isValidCode(String code) {
        return code != null && code.matches("[A-Z0-9]{3,20}");
    }

    /**
     * Validate discount percentage
     */
    public static boolean isValidPercentage(Double percentage) {
        return percentage != null && percentage > 0 && percentage <= 100;
    }
}