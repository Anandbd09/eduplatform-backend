// FILE 20: CacheKeyGenerator.java
package com.eduplatform.cache.util;

import java.security.MessageDigest;
import java.util.UUID;

public class CacheKeyGenerator {

    /**
     * GENERATE CACHE KEY (TYPE:ID format)
     */
    public static String generateKey(String cacheType, String identifier) {
        return cacheType.toUpperCase() + ":" + identifier;
    }

    /**
     * GENERATE HASH OF VALUE (SHA256)
     */
    public static String generateHash(Object value) {
        try {
            String valueStr = value.toString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(valueStr.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }
}