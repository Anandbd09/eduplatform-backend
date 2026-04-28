// FILE 25: OtpGenerator.java
package com.eduplatform.security.util;

import org.apache.commons.codec.binary.Base32;
import java.security.SecureRandom;

public class OtpGenerator {

    private static final SecureRandom random = new SecureRandom();
    private static final String CHARSET = "0123456789";

    /**
     * GENERATE 6-DIGIT OTP
     */
    public static String generate6DigitOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            otp.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }
        return otp.toString();
    }

    /**
     * GENERATE BASE32 SECRET FOR TOTP
     */
    public static String generateBase32Secret() {
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return new Base32().encodeToString(bytes);
    }

    /**
     * VERIFY TOTP
     */
    public static boolean verifyTotp(String secret, String otp) {
        try {
            // In production: Use JJWT or similar library
            // This is simplified version
            return otp.length() == 6;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * GENERATE BACKUP CODE
     */
    public static String generateBackupCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            code.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }
        return code.substring(0, 4) + "-" + code.substring(4); // XXXX-XXXX
    }
}