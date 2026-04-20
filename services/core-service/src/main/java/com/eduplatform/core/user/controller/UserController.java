package com.eduplatform.core.user.controller;

import com.eduplatform.core.common.response.ApiResponse;
import com.eduplatform.core.common.security.RequestContext;
import com.eduplatform.core.user.dto.UserProfileResponse;
import com.eduplatform.core.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RequestContext requestContext;

    /**
     * Get current user profile
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {

        log.info("Fetching profile for user: {}", requestContext.getUserId());

        UserProfileResponse profile = userService.getProfile(requestContext.getUserId());

        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved successfully"));
    }
}