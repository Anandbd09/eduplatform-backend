// FILE 23: CacheValidator.java
package com.eduplatform.cache.util;

public class CacheValidator {

    /**
     * VALIDATE CACHE KEY FORMAT
     */
    public static boolean isValidCacheKey(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        return key.matches("^[A-Z_]+:[a-zA-Z0-9_-]+$");
    }

    /**
     * VALIDATE TTL SECONDS
     */
    public static boolean isValidTtl(Integer ttlSeconds) {
        return ttlSeconds != null && ttlSeconds > 0 && ttlSeconds <= 86400;
    }

    /**
     * VALIDATE EVICTION POLICY
     */
    public static boolean isValidEvictionPolicy(String policy) {
        return policy != null && (
                "LRU".equals(policy) ||
                        "LFU".equals(policy) ||
                        "FIFO".equals(policy) ||
                        "TTL".equals(policy)
        );
    }
}