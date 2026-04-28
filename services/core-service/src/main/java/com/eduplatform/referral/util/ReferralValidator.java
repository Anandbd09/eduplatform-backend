// FILE 27: ReferralValidator.java
package com.eduplatform.referral.util;

public class ReferralValidator {

    /**
     * VALIDATE REFERRAL CODE FORMAT
     */
    public static boolean isValidReferralCode(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        return code.matches("REF-[A-Z0-9]{5}-[A-Z0-9]{5}");
    }

    /**
     * VALIDATE EMAIL
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * VALIDATE COURSE PRICE
     */
    public static boolean isValidPrice(Double price) {
        return price != null && price > 0;
    }
}