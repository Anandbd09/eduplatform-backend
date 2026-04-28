package com.eduplatform.social.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "user_follows")
public class UserFollow {

    @Id
    private String id;

    @Indexed
    private String followerId;

    @Indexed
    private String followingId;

    @Indexed(unique = true)
    private String relationshipKey; // "userId1:userId2"

    private String status; // ACTIVE, BLOCKED

    private Boolean isPublic; // Public or private follow

    private Boolean notificationsEnabled; // Get notifs from this user

    @Indexed
    private LocalDateTime followedAt;

    private LocalDateTime unfollowedAt;

    private String notes; // Why following (optional)

    private Long mutualFollowersCount;

    private Long sharedCoursesCount;

    @Indexed
    private String tenantId;

    private Long version_field = 0L;

    /**
     * Is follow still active
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /**
     * Is blocked
     */
    public boolean isBlocked() {
        return "BLOCKED".equals(status);
    }
}