package com.eduplatform.batch.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class UserImportService {

    /**
     * IMPORT USER FROM CSV
     */
    public void importUser(Map<String, String> userData, String userType, String tenantId) {
        try {
            String email = userData.get("email");
            String firstName = userData.get("firstName");
            String lastName = userData.get("lastName");

            // In production: Call user service to create/update user
            // userService.createUser(User.builder()
            //    .email(email)
            //    .firstName(firstName)
            //    .lastName(lastName)
            //    .role(userType)
            //    .tenantId(tenantId)
            //    .build());

            log.info("User imported: email={}, type={}", email, userType);

        } catch (Exception e) {
            log.error("Error importing user", e);
            throw new RuntimeException("Failed to import user");
        }
    }
}