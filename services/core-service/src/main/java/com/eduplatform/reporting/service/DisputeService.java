package com.eduplatform.reporting.service;

import com.eduplatform.reporting.model.*;
import com.eduplatform.reporting.repository.*;
import com.eduplatform.reporting.dto.*;
import com.eduplatform.reporting.exception.ReportingException;
import com.eduplatform.reporting.util.PriorityCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class DisputeService {

    @Autowired
    private DisputeRepository disputeRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Create dispute from report
     */
    public DisputeResponse createDispute(DisputeRequest request, String tenantId) {
        try {
            // Get report
            Report report = reportRepository.findById(request.getReportId())
                    .orElseThrow(() -> new ReportingException("Report not found", "REPORT_NOT_FOUND", 404));

            if (!tenantId.equals(report.getTenantId())) {
                throw new ReportingException("Unauthorized", "UNAUTHORIZED", 403);
            }

            // Check if dispute already exists
            Optional<Dispute> existing = disputeRepository.findByReportIdAndTenantId(request.getReportId(), tenantId);
            if (existing.isPresent()) {
                throw new ReportingException("Dispute already exists for this report", "DUPLICATE_DISPUTE");
            }

            // Calculate priority
            int priority = PriorityCalculator.calculatePriority(
                    report.getSeverity(),
                    report.getCategory(),
                    report.getEvidenceUrls() != null ? report.getEvidenceUrls().size() : 0
            );

            // Create dispute
            Dispute dispute = Dispute.builder()
                    .id(UUID.randomUUID().toString())
                    .reportId(request.getReportId())
                    .disputedUserId(request.getDisputedUserId())
                    .disputedUserName(request.getDisputedUserName())
                    .disputedUserEmail(request.getDisputedUserEmail())
                    .reason(request.getReason())
                    .priority(priority)
                    .queuePosition(getNextQueuePosition(tenantId))
                    .status("QUEUED")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .version(0L)
                    .build();

            Dispute saved = disputeRepository.save(dispute);

            // Update report status
            report.setStatus("UNDER_REVIEW");
            reportRepository.save(report);

            log.info("Dispute created: {} from report: {}", saved.getId(), request.getReportId());

            // Notify disputed user
            notificationService.notifyDisputeCreated(saved);

            return convertToResponse(saved);

        } catch (ReportingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating dispute", e);
            throw new ReportingException("Failed to create dispute");
        }
    }

    /**
     * Get admin queue
     */
    public Page<DisputeResponse> getAdminQueue(int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);

            Pageable pageable = PageRequest.of(page, size,
                    Sort.by("priority").descending()
                            .and(Sort.by("createdAt").ascending()));

            return disputeRepository.findQueuedDisputes(tenantId, pageable)
                    .map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error fetching admin queue", e);
            throw new ReportingException("Failed to fetch queue");
        }
    }

    /**
     * Assign dispute to admin
     */
    public DisputeResponse assignDispute(String disputeId, String adminId, String adminName, String tenantId) {
        try {
            Dispute dispute = disputeRepository.findById(disputeId)
                    .orElseThrow(() -> new ReportingException("Dispute not found", "DISPUTE_NOT_FOUND", 404));

            if (!dispute.getTenantId().equals(tenantId)) {
                throw new ReportingException("Unauthorized", "UNAUTHORIZED", 403);
            }

            dispute.setAssignedTo(adminId);
            dispute.setAssignedToName(adminName);
            dispute.setAssignedAt(LocalDateTime.now());
            dispute.setStatus("ASSIGNED");
            dispute.setResponseDeadline(LocalDateTime.now().plusDays(7));
            dispute.setUpdatedAt(LocalDateTime.now());

            Dispute saved = disputeRepository.save(dispute);

            log.info("Dispute {} assigned to admin: {}", disputeId, adminId);

            // Notify disputed user of assignment
            notificationService.notifyDisputeAssigned(saved);

            return convertToResponse(saved);

        } catch (ReportingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error assigning dispute", e);
            throw new ReportingException("Failed to assign dispute");
        }
    }

    /**
     * Get disputes for admin
     */
    public Page<DisputeResponse> getAdminDisputes(String adminId, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());

            return disputeRepository.findByAssignedToAndTenantId(adminId, tenantId, pageable)
                    .map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error fetching admin disputes", e);
            throw new ReportingException("Failed to fetch disputes");
        }
    }

    /**
     * Submit user response
     */
    public DisputeResponse submitUserResponse(String disputeId, String response, String userId, String tenantId) {
        try {
            Dispute dispute = disputeRepository.findById(disputeId)
                    .orElseThrow(() -> new ReportingException("Dispute not found", "DISPUTE_NOT_FOUND", 404));

            if (!dispute.getTenantId().equals(tenantId)) {
                throw new ReportingException("Unauthorized", "UNAUTHORIZED", 403);
            }

            if (!dispute.getDisputedUserId().equals(userId)) {
                throw new ReportingException("Only disputed user can respond", "UNAUTHORIZED", 403);
            }

            if (dispute.isDeadlinePassed()) {
                throw new ReportingException("Response deadline has passed", "DEADLINE_PASSED");
            }

            dispute.setUserResponse(response);
            dispute.setResponseReceivedAt(LocalDateTime.now());
            dispute.setStatus("AWAITING_RESPONSE");
            dispute.setUpdatedAt(LocalDateTime.now());

            Dispute saved = disputeRepository.save(dispute);

            log.info("User response submitted for dispute: {}", disputeId);

            // Notify admin
            notificationService.notifyUserResponseReceived(saved);

            return convertToResponse(saved);

        } catch (ReportingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error submitting response", e);
            throw new ReportingException("Failed to submit response");
        }
    }

    /**
     * Update admin notes
     */
    public DisputeResponse updateAdminNotes(String disputeId, String notes, String adminId, String tenantId) {
        try {
            Dispute dispute = disputeRepository.findById(disputeId)
                    .orElseThrow(() -> new ReportingException("Dispute not found", "DISPUTE_NOT_FOUND", 404));

            if (!dispute.getTenantId().equals(tenantId)) {
                throw new ReportingException("Unauthorized", "UNAUTHORIZED", 403);
            }

            if (dispute.getAssignedTo() == null || !dispute.getAssignedTo().equals(adminId)) {
                throw new ReportingException("Only assigned admin can update notes", "UNAUTHORIZED", 403);
            }

            dispute.setAdminNotes(notes);
            dispute.setUpdatedAt(LocalDateTime.now());

            Dispute saved = disputeRepository.save(dispute);

            log.info("Admin notes updated for dispute: {}", disputeId);

            return convertToResponse(saved);

        } catch (ReportingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating notes", e);
            throw new ReportingException("Failed to update notes");
        }
    }

    /**
     * Get overdue disputes
     */
    public List<DisputeResponse> getOverdueDisputes(String tenantId) {
        try {
            return disputeRepository.findOverdueDisputes(tenantId).stream()
                    .map(this::convertToResponse)
                    .toList();
        } catch (Exception e) {
            log.error("Error fetching overdue disputes", e);
            throw new ReportingException("Failed to fetch overdue disputes");
        }
    }

    /**
     * Get disputes for disputed user
     */
    public Page<DisputeResponse> getDisputesAgainstUser(String userId, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            return disputeRepository.findByDisputedUserIdAndTenantId(userId, tenantId, pageable)
                    .map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error fetching disputes against user", e);
            throw new ReportingException("Failed to fetch disputes");
        }
    }

    /**
     * Get next queue position
     */
    private Integer getNextQueuePosition(String tenantId) {
        List<Dispute> queued = disputeRepository.findQueuedOrderByPriority(tenantId);
        return queued.size() + 1;
    }

    /**
     * Convert to response
     */
    private DisputeResponse convertToResponse(Dispute dispute) {
        return DisputeResponse.builder()
                .id(dispute.getId())
                .reportId(dispute.getReportId())
                .disputedUserId(dispute.getDisputedUserId())
                .disputedUserName(dispute.getDisputedUserName())
                .priority(dispute.getPriority())
                .status(dispute.getStatus())
                .assignedTo(dispute.getAssignedTo())
                .assignedToName(dispute.getAssignedToName())
                .responseDeadline(dispute.getResponseDeadline())
                .daysRemaining(dispute.getStatus().equals("ASSIGNED") ? dispute.getDaysRemaining() : null)
                .createdAt(dispute.getCreatedAt())
                .build();
    }
}
