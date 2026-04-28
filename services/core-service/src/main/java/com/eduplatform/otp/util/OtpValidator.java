// FILE 18: OtpValidator.java
package com.eduplatform.otp.util;

import com.eduplatform.otp.exception.OtpException;

public class OtpValidator {

    /**
     * VALIDATE OTP CODE
     */
    public static void validateOtpCode(String code) throws OtpException {
        if (code == null || code.isEmpty()) {
            throw new OtpException("OTP code is required");
        }

        if (!code.matches("^\\d{6}$")) {
            throw new OtpException("OTP must be exactly 6 digits");
        }
    }

    /**
     * VALIDATE PURPOSE
     */
    public static void validatePurpose(String purpose) throws OtpException {
        if (purpose == null || purpose.isEmpty()) {
            throw new OtpException("OTP purpose is required");
        }

        if (!purpose.matches("PHONE_VERIFICATION|EMAIL_VERIFICATION|PASSWORD_RESET")) {
            throw new OtpException("Invalid OTP purpose");
        }
    }
}