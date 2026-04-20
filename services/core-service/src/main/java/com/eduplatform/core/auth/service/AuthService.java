package com.eduplatform.core.auth.service;

import com.eduplatform.core.auth.dto.LoginRequest;
import com.eduplatform.core.auth.dto.LoginResponse;
import com.eduplatform.core.auth.dto.RegisterRequest;
import com.eduplatform.core.auth.dto.RegisterResponse;
import com.eduplatform.core.auth.util.TokenProvider;
import com.eduplatform.core.common.exception.AppException;
import com.eduplatform.core.user.model.User;
import com.eduplatform.core.user.repository.UserRepository;
import com.eduplatform.notification.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.security.SecureRandom;

@Slf4j
@Service
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${app.frontend-reset-url:http://localhost:8084/reset-password}")
    private String frontendResetUrl;

    @Value("${app.auth.password-reset-expiry-minutes:30}")
    private long passwordResetExpiryMinutes;

    /**
     * Register a new user
     */
    public RegisterResponse register(RegisterRequest request) {

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw AppException.conflict("Email already registered");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole() != null ? request.getRole() : "STUDENT")
                .status("ACTIVE")
                .tenantId("default")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        // Generate tokens
        String accessToken = tokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole(), user.getTenantId()
        );
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        log.info("User registered: {} ({})", user.getEmail(), user.getId());

        return RegisterResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiryMinutes(tokenProvider.getAccessTokenExpiryMinutes())
                .build();
    }

    /**
     * Login user with email and password
     */
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> AppException.unauthorized("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw AppException.unauthorized("Invalid email or password");
        }

        // Update last login time
        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Generate tokens
        String accessToken = tokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole(), user.getTenantId()
        );
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        log.info("User logged in: {} ({})", user.getEmail(), user.getId());

        return LoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiryMinutes(tokenProvider.getAccessTokenExpiryMinutes())
                .build();
    }

    /**
     * Generate new access token using refresh token
     */
    public LoginResponse refreshToken(String refreshToken) {
        String userId = tokenProvider.getUserIdFromToken(refreshToken);
        if (userId == null) {
            throw AppException.unauthorized("Invalid or expired refresh token");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> AppException.unauthorized("User not found for refresh token"));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw AppException.unauthorized("User account is not active");
        }

        String newAccessToken = tokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole(), user.getTenantId()
        );
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getId());

        log.info("Access token refreshed for user: {} ({})", user.getEmail(), user.getId());

        return LoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessTokenExpiryMinutes(tokenProvider.getAccessTokenExpiryMinutes())
                .build();
    }

    /**
     * Logout user (blacklist token)
     */
    public void logout(String accessToken) {
        // TODO: Add token to blacklist in Redis
        // TODO: Invalidate refresh token in MongoDB

        log.info("User logged out");
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            log.info("Password reset requested for non-existent email: {}", email);
            return;
        }

        User user = userOptional.get();
        String resetToken = generateResetToken();

        user.setPasswordResetTokenHash(hashToken(resetToken));
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusMinutes(passwordResetExpiryMinutes));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        emailService.sendPasswordResetEmail(
                user.getEmail(),
                user.getFirstName(),
                buildResetUrl(resetToken),
                passwordResetExpiryMinutes
        );

        log.info("Password reset email sent to: {}", email);
    }

    /**
     * Reset password using reset token
     */
    public void resetPassword(String resetToken, String newPassword) {
        if (resetToken == null || resetToken.isBlank()) {
            throw AppException.badRequest("Reset token is required");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw AppException.badRequest("New password is required");
        }

        User user = userRepository.findByPasswordResetTokenHash(hashToken(resetToken))
                .orElseThrow(() -> AppException.badRequest("Invalid or expired reset token"));

        if (user.getPasswordResetExpiresAt() == null ||
                user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            clearPasswordResetState(user);
            userRepository.save(user);
            throw AppException.badRequest("Invalid or expired reset token");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        clearPasswordResetState(user);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Password reset successful");
    }

    private String buildResetUrl(String resetToken) {
        String separator = frontendResetUrl.contains("?") ? "&" : "?";
        return frontendResetUrl + separator + "token=" + resetToken;
    }

    private String generateResetToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw AppException.internalError("Unable to process reset token");
        }
    }

    private void clearPasswordResetState(User user) {
        user.setPasswordResetTokenHash(null);
        user.setPasswordResetExpiresAt(null);
    }
}
