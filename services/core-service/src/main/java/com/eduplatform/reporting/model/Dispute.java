package com.eduplatform.reporting.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "disputes")
@CompoundIndex(name = "reportId_idx", def = "{'reportId': 1, 'tenantId': 1}", unique = true)
public class Dispute {

    @Id
    private String id;

    @Indexed(unique = true)
    private String reportId; // Reference to Report

    @Indexed
    private String disputedUserId; // User being disputed against

    private String disputedUserName;

    private String disputedUserEmail;

    private String reason; // Summary of dispute

    @Indexed
    private Integer priority; // 1-10, higher = more urgent

    @Indexed
    private Integer queuePosition; // Position in admin queue

    @Indexed
    private String status; // QUEUED, ASSIGNED, INVESTIGATING, AWAITING_RESPONSE, RESOLVED

    private String assignedTo; // Admin ID

    private String assignedToName;

    private LocalDateTime assignedAt;

    @Indexed
    private LocalDateTime responseDeadline; // 7 days from assignment

    private String userResponse; // Disputed user's response

    private LocalDateTime responseReceivedAt;

    private String adminNotes; // Investigation notes

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime updatedAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Check if response deadline passed
     */
    public boolean isDeadlinePassed() {
        return responseDeadline != null && LocalDateTime.now().isAfter(responseDeadline);
    }

    /**
     * Calculate days remaining
     */
    public long getDaysRemaining() {
        if (responseDeadline == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), responseDeadline);
    }
}