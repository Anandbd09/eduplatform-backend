package com.eduplatform.core.auth.controller;

import com.eduplatform.core.auth.dto.LoginRequest;
import com.eduplatform.core.auth.dto.LoginResponse;
import com.eduplatform.core.auth.dto.RegisterRequest;
import com.eduplatform.core.auth.dto.RegisterResponse;
import com.eduplatform.core.auth.service.AuthService;
import com.eduplatform.core.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Register request for email: {}", request.getEmail());

        RegisterResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User registered successfully"));
    }

    /**
     * Login user
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request for email: {}", request.getEmail());

        LoginResponse response = authService.login(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Login successful"));
    }

    /**
     * Refresh access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @RequestHeader("Authorization") String bearerToken) {

        String refreshToken = bearerToken.substring(7);
        LoginResponse response = authService.refreshToken(refreshToken);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Token refreshed successfully"));
    }

    /**
     * Logout user
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String bearerToken) {

        String accessToken = bearerToken.substring(7);
        authService.logout(accessToken);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(null, "Logout successful"));
    }

    /**
     * Forgot password - send reset email
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @RequestParam String email) {

        log.info("Forgot password request for: {}", email);

        authService.sendPasswordResetEmail(email);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(null, "Password reset email sent"));
    }

    /**
     * Reset password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestParam String resetToken,
            @RequestParam String newPassword) {

        authService.resetPassword(resetToken, newPassword);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(null, "Password reset successfully"));
    }
}