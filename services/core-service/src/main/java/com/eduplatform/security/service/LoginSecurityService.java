package com.eduplatform.security.service;

import com.eduplatform.security.repository.LoginAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
public class LoginSecurityService {

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    private static final Integer MAX_ATTEMPTS = 5;
    private static final Integer LOCKOUT_MINUTES = 30;

    /**
     * CHECK IF ACCOUNT LOCKED
     */
    public boolean isAccountLocked(String userId, String tenantId) {
        Long failedAttempts = loginAttemptRepository.countByUserIdAndStatusAndAttemptedAtAfterAndTenantId(
                userId, "FAILED", LocalDateTime.now().minusMinutes(LOCKOUT_MINUTES), tenantId);

        return failedAttempts >= MAX_ATTEMPTS;
    }

    /**
     * GET FAILED ATTEMPTS COUNT
     */
    public Long getFailedAttemptsCount(String userId, String tenantId) {
        return loginAttemptRepository.countByUserIdAndStatusAndAttemptedAtAfterAndTenantId(
                userId, "FAILED", LocalDateTime.now().minusMinutes(LOCKOUT_MINUTES), tenantId);
    }
}