package com.eduplatform.core.user.service;

import com.eduplatform.core.common.exception.AppException;
import com.eduplatform.core.user.dto.UserProfileResponse;
import com.eduplatform.core.user.model.User;
import com.eduplatform.core.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Get user profile by ID
     */
    public UserProfileResponse getProfile(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> AppException.notFound("User not found"));

        return UserProfileResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole())
                .profilePicture(user.getProfilePicture())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}