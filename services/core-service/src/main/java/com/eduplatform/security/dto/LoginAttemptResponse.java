// FILE 21: LoginAttemptResponse.java
package com.eduplatform.security.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginAttemptResponse {
    private String status;
    private String ipAddress;
    private String country;
    private String deviceId;
    private LocalDateTime attemptedAt;
}