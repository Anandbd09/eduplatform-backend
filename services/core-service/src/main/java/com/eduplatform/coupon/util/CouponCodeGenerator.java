// FILE 25: CouponCodeGenerator.java
package com.eduplatform.coupon.util;

import java.util.Random;

public class CouponCodeGenerator {

    /**
     * Generate random coupon code
     */
    public static String generateCode(String prefix, int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();

        if (prefix != null && !prefix.isEmpty()) {
            code.append(prefix);
        }

        Random random = new Random();
        for (int i = code.length(); i < length; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        return code.toString();
    }

    /**
     * Generate code with default length 12
     */
    public static String generateCode(String prefix) {
        return generateCode(prefix, 12);
    }

    /**
     * Generate code without prefix
     */
    public static String generateCode() {
        return generateCode("", 12);
    }
}