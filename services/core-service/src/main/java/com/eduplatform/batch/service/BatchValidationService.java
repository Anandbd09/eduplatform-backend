package com.eduplatform.batch.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BatchValidationService {

    /**
     * VALIDATE USER DATA
     */
    public List<String> validateUser(Map<String, String> userData, String userType) {
        List<String> errors = new ArrayList<>();

        String email = userData.get("email");
        String firstName = userData.get("firstName");
        String lastName = userData.get("lastName");
        String password = userData.get("password");

        if (email == null || email.isBlank()) {
            errors.add("Email is required");
        } else if (!isValidEmail(email)) {
            errors.add("Invalid email format");
        }

        if (firstName == null || firstName.isBlank()) {
            errors.add("First name is required");
        }

        if (lastName == null || lastName.isBlank()) {
            errors.add("Last name is required");
        }

        if (password == null || password.length() < 8) {
            errors.add("Password must be at least 8 characters");
        }

        return errors;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}