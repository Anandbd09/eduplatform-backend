package com.eduplatform.core.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String accessToken;
    private String refreshToken;
    private int accessTokenExpiryMinutes;
}