// FILE 26: RewardCalculator.java
package com.eduplatform.referral.util;

public class RewardCalculator {

    private static final Double REWARD_PERCENTAGE = 0.20; // 20% of course price
    private static final Double PLATFORM_FEE = 0.05; // 5% payout fee

    /**
     * CALCULATE REWARD (20% OF COURSE PRICE)
     */
    public static Double calculateReward(Double coursePrice) {
        if (coursePrice == null || coursePrice <= 0) {
            return 0.0;
        }
        return coursePrice * REWARD_PERCENTAGE;
    }

    /**
     * CALCULATE NET PAYOUT (95% AFTER 5% FEE)
     */
    public static Double calculateNetPayout(Double totalAmount) {
        if (totalAmount == null || totalAmount <= 0) {
            return 0.0;
        }
        return totalAmount * (1 - PLATFORM_FEE);
    }

    /**
     * CALCULATE PLATFORM FEE
     */
    public static Double calculatePlatformFee(Double totalAmount) {
        if (totalAmount == null || totalAmount <= 0) {
            return 0.0;
        }
        return totalAmount * PLATFORM_FEE;
    }
}