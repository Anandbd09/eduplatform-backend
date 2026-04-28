// FILE 25: ReferralCodeGenerator.java
package com.eduplatform.referral.util;
import java.security.SecureRandom;

public class ReferralCodeGenerator {

    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();

    /**
     * GENERATE UNIQUE REFERRAL CODE: REF-XXXXX-XXXXX (12 chars)
     */
    public static String generateCode() {
        StringBuilder code = new StringBuilder("REF-");

        // First part: 5 chars
        for (int i = 0; i < 5; i++) {
            code.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }

        code.append("-");

        // Second part: 5 chars
        for (int i = 0; i < 5; i++) {
            code.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }

        return code.toString();
    }
}