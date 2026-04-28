// FILE 19: RateLimiter.java
package com.eduplatform.otp.util;

import com.eduplatform.otp.exception.OtpException;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {

    private static final ConcurrentHashMap<String, Integer> otpAttempts = new ConcurrentHashMap<>();
    private static final int MAX_OTP_PER_HOUR = 3;

    /**
     * CHECK RATE LIMIT (Max 3 OTPs per hour per user per type)
     */
    public static void checkRateLimit(String userId, String type, String tenantId) throws OtpException {
        String key = userId + ":" + type + ":" + tenantId;

        int attempts = otpAttempts.getOrDefault(key, 0);

        if (attempts >= MAX_OTP_PER_HOUR) {
            throw new OtpException("Too many OTP attempts. Please try again later.");
        }

        otpAttempts.put(key, attempts + 1);

        // In production: use Redis with TTL
    }
}