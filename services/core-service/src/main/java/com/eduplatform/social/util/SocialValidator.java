// FILE 30: SocialValidator.java
package com.eduplatform.social.util;

import com.eduplatform.social.exception.SocialException;

public class SocialValidator {

    /**
     * VALIDATE USER ID
     */
    public static void validateUserId(String userId) throws SocialException {
        if (userId == null || userId.isEmpty()) {
            throw new SocialException("User ID cannot be empty");
        }
    }
}