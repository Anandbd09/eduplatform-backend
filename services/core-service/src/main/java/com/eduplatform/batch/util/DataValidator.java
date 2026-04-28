// FILE 32: DataValidator.java
package com.eduplatform.batch.util;

import java.util.ArrayList;
import java.util.List;

public class DataValidator {

    /**
     * VALIDATE EMAIL
     */
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * VALIDATE PHONE
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^[0-9]{10}$");
    }

    /**
     * VALIDATE PASSWORD STRENGTH
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }
}