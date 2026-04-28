// FILE 26: DiscountCalculator.java
package com.eduplatform.coupon.util;

public class DiscountCalculator {

    /**
     * Calculate discount amount based on type
     */
    public static Double calculateDiscount(String discountType, Double amount,
                                           Double discountValue, Double maxDiscount) {

        if ("PERCENTAGE".equals(discountType)) {
            // Calculate percentage discount
            Double discount = (amount * discountValue) / 100;

            // Apply max discount limit if set
            if (maxDiscount != null && discount > maxDiscount) {
                return maxDiscount;
            }

            return discount;
        } else if ("FIXED".equals(discountType)) {
            // Fixed discount amount
            // Don't exceed the original amount
            return Math.min(discountValue, amount);
        }

        return 0.0;
    }

    /**
     * Calculate final amount after discount
     */
    public static Double calculateFinalAmount(Double originalAmount, Double discountAmount) {
        return Math.max(0.0, originalAmount - discountAmount);
    }

    /**
     * Calculate effective discount percentage
     */
    public static Double calculateEffectivePercentage(Double originalAmount, Double discountAmount) {
        if (originalAmount == 0) {
            return 0.0;
        }
        return (discountAmount / originalAmount) * 100;
    }
}