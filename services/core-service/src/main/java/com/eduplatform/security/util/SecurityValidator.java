// FILE 28: SecurityValidator.java
package com.eduplatform.security.util;

public class SecurityValidator {

    /**
     * VALIDATE IP ADDRESS
     */
    public static boolean isValidIpAddress(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        return ip.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");
    }

    /**
     * VALIDATE CIDR BLOCK
     */
    public static boolean isValidCidrBlock(String cidr) {
        if (cidr == null || !cidr.contains("/")) {
            return false;
        }

        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            return false;
        }

        try {
            int prefix = Integer.parseInt(parts[1]);
            return isValidIpAddress(parts[0]) && prefix >= 0 && prefix <= 32;
        } catch (Exception e) {
            return false;
        }
    }
}