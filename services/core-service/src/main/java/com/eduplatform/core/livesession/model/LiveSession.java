package com.eduplatform.core.livesession.model;

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

@Document(collection = "liveSessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveSession {

    @Id
    private String id;

    private String tenantId;

    private String courseId;

    private String instructorId;

//    @Indexed(unique = true)
    private String roomId;

    private String title;

    private String description;

    private LocalDateTime scheduledAt;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private String status;  // SCHEDULED, LIVE, ENDED, CANCELLED

    private boolean isRecorded;

    private String recordingUrl;

    private int maxParticipants;

    @Builder.Default
    private List<Participant> participants = new ArrayList<>();

    private boolean isChatEnabled;

    private boolean isPublic;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Participant {

        private String userId;

        private String name;

        private String role;  // INSTRUCTOR, STUDENT

        private boolean isMuted;

        private boolean isCameraOff;

        private LocalDateTime joinedAt;

        private LocalDateTime leftAt;
    }
}