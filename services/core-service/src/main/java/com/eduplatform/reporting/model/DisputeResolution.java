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
@Document(collection = "dispute_resolutions")
@CompoundIndex(name = "disputeId_idx", def = "{'disputeId': 1, 'tenantId': 1}", unique = true)
public class DisputeResolution {

    @Id
    private String id;

    @Indexed(unique = true)
    private String disputeId;

    private String resolvedBy; // Admin ID

    private String resolvedByName;

    @Indexed
    private String decision; // UPHELD, DISMISSED, PARTIAL

    private String decisionReason;

    private String consequences; // Action taken

    // ACTION DETAILS
    private String actionType; // WARNING, SUSPENSION, REMOVAL, NONE

    private Integer suspensionDays; // If suspended

    private String suspensionReason;

    // APPEAL DETAILS
    private Boolean isAppealable = true;

    private LocalDateTime appealDeadline; // 7 days from resolution

    private Boolean appealed = false;

    private LocalDateTime appealedAt;

    private String appealReason;

    @Indexed
    private String finalDecision; // UPHELD, DISMISSED, PARTIAL (after appeal if any)

    @Indexed
    private LocalDateTime resolvedAt;

    private LocalDateTime finalResolvedAt; // After appeal resolution

    private String publicSummary; // What to show publicly

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Check if appeal is still possible
     */
    public boolean canAppeal() {
        if (!isAppealable || appealed) {
            return false;
        }
        return appealDeadline != null && LocalDateTime.now().isBefore(appealDeadline);
    }

    /**
     * Get days remaining for appeal
     */
    public long getAppealDaysRemaining() {
        if (appealDeadline == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), appealDeadline);
    }
}