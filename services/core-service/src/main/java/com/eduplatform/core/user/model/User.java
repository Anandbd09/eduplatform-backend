package com.eduplatform.core.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    private String firstName;

    private String lastName;

    private String phone;

    private String role;  // STUDENT, INSTRUCTOR, ADMIN

    private String status;  // ACTIVE, INACTIVE, BANNED, SUSPENDED, DELETED

    private LocalDateTime bannedUntil;

    private String banReason;

    private LocalDateTime bannedAt;

    private LocalDateTime suspendedUntil;

    private String suspensionReason;

    private String tenantId;

    private String profilePicture;

    private String bio;

    @Builder.Default
    private List<String> deviceSessions = new ArrayList<>();

    @Indexed(sparse = true)
    private String passwordResetTokenHash;

    private LocalDateTime passwordResetExpiresAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;

    private LocalDateTime deletedAt;
}
