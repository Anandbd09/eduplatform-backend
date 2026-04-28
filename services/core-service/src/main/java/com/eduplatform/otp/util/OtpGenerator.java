// FILE 17: OtpGenerator.java
package com.eduplatform.otp.util;

import java.security.SecureRandom;

public class OtpGenerator {

    private static final SecureRandom random = new SecureRandom();

    /**
     * GENERATE 6-DIGIT OTP
     */
    public static String generate() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}