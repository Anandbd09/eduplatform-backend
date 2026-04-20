package com.eduplatform.core.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String role;
    private String profilePicture;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}