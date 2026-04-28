package com.eduplatform.reporting.service;

import com.eduplatform.reporting.model.*;
import com.eduplatform.reporting.repository.*;
import com.eduplatform.reporting.dto.*;
import com.eduplatform.reporting.exception.ReportingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class AdminReviewService {

    @Autowired
    private DisputeRepository disputeRepository;

    @Autowired
    private DisputeResolutionRepository resolutionRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Resolve dispute
     */
    public DisputeResolutionResponse resolveDispute(String disputeId,
                                                    DisputeResolutionRequest request,
                                                    String adminId, String tenantId) {
        try {
            Dispute dispute = disputeRepository.findById(disputeId)
                    .orElseThrow(() -> new ReportingException("Dispute not found", "DISPUTE_NOT_FOUND", 404));

            if (!dispute.getTenantId().equals(tenantId)) {
                throw new ReportingException("Unauthorized", "UNAUTHORIZED", 403);
            }

            if (dispute.getAssignedTo() == null || !dispute.getAssignedTo().equals(adminId)) {
                throw new ReportingException("Only assigned admin can resolve this dispute", "UNAUTHORIZED", 403);
            }

            if ("RESOLVED".equals(dispute.getStatus())) {
                throw new ReportingException("Dispute already resolved", "DISPUTE_ALREADY_RESOLVED", 409);
            }

            if (resolutionRepository.findByDisputeIdAndTenantId(disputeId, tenantId).isPresent()) {
                throw new ReportingException("Resolution already exists", "RESOLUTION_ALREADY_EXISTS", 409);
            }

            // Create resolution
            DisputeResolution resolution = DisputeResolution.builder()
                    .id(UUID.randomUUID().toString())
                    .disputeId(disputeId)
                    .resolvedBy(adminId)
                    .resolvedByName(request.getResolvedByName())
                    .decision(request.getDecision())
                    .decisionReason(request.getDecisionReason())
                    .consequences(request.getConsequences())
                    .actionType(request.getActionType())
                    .suspensionDays(request.getSuspensionDays())
                    .isAppealable(true)
                    .appealDeadline(LocalDateTime.now().plusDays(7))
                    .finalDecision(request.getDecision())
                    .resolvedAt(LocalDateTime.now())
                    .publicSummary(request.getPublicSummary())
                    .tenantId(tenantId)
                    .version(0L)
                    .build();

            DisputeResolution saved = resolutionRepository.save(resolution);

            // Update dispute
            dispute.setStatus("RESOLVED");
            dispute.setUpdatedAt(LocalDateTime.now());
            disputeRepository.save(dispute);

            // Update report
            Report report = reportRepository.findById(dispute.getReportId())
                    .orElse(null);
            if (report != null) {
                report.setStatus("RESOLVED");
                report.setResolutionNotes(request.getConsequences());
                report.setResolvedAt(LocalDateTime.now());
                reportRepository.save(report);
            }

            log.info("Dispute {} resolved with decision: {}", disputeId, request.getDecision());

            // Send notifications
            notificationService.notifyDisputeResolved(dispute, saved);

            return convertToResponse(saved);

        } catch (ReportingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error resolving dispute", e);
            throw new ReportingException("Failed to resolve dispute");
        }
    }

    /**
     * Get resolution
     */
    public DisputeResolutionResponse getResolution(String disputeId, String tenantId) {
        try {
            DisputeResolution resolution = resolutionRepository.findByDisputeIdAndTenantId(disputeId, tenantId)
                    .orElseThrow(() -> new ReportingException("Resolution not found", "RESOLUTION_NOT_FOUND", 404));

            return convertToResponse(resolution);

        } catch (ReportingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching resolution", e);
            throw new ReportingException("Failed to fetch resolution");
        }
    }

    /**
     * Appeal resolution
     */
    public DisputeResolutionResponse appealResolution(String disputeId, String appealReason,
                                                      String userId, String tenantId) {
        try {
            DisputeResolution resolution = resolutionRepository.findByDisputeIdAndTenantId(disputeId, tenantId)
                    .orElseThrow(() -> new ReportingException("Resolution not found", "RESOLUTION_NOT_FOUND", 404));

            if (!resolution.canAppeal()) {
                throw new ReportingException("Appeal window has closed", "APPEAL_NOT_ALLOWED");
            }

            Dispute dispute = disputeRepository.findById(disputeId)
                    .orElse(null);

            if (dispute != null && !dispute.getDisputedUserId().equals(userId)) {
                throw new ReportingException("Only disputed user can appeal", "UNAUTHORIZED", 403);
            }

            resolution.setAppealed(true);
            resolution.setAppealedAt(LocalDateTime.now());
            resolution.setAppealReason(appealReason);

            DisputeResolution saved = resolutionRepository.save(resolution);

            log.info("Resolution {} appealed", disputeId);

            // Notify admins of appeal
            notificationService.notifyAppealSubmitted(resolution);

            return convertToResponse(saved);

        } catch (ReportingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error appealing resolution", e);
            throw new ReportingException("Failed to submit appeal");
        }
    }

    /**
     * Get resolutions by decision
     */
    public Page<DisputeResolutionResponse> getResolutionsByDecision(String decision, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("resolvedAt").descending());

            return resolutionRepository.findByDecisionAndTenantId(decision, tenantId, pageable)
                    .map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error fetching resolutions", e);
            throw new ReportingException("Failed to fetch resolutions");
        }
    }

    /**
     * Get appealed resolutions
     */
    public Page<DisputeResolutionResponse> getAppealedResolutions(int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("appealedAt").descending());

            return resolutionRepository.findByAppealedTrueAndTenantId(tenantId, pageable)
                    .map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error fetching appealed resolutions", e);
            throw new ReportingException("Failed to fetch resolutions");
        }
    }

    /**
     * Get resolutions by action type
     */
    public Page<DisputeResolutionResponse> getResolutionsByActionType(String actionType, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("resolvedAt").descending());

            return resolutionRepository.findByActionTypeAndTenantId(actionType, tenantId, pageable)
                    .map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error fetching resolutions by action", e);
            throw new ReportingException("Failed to fetch resolutions");
        }
    }

    /**
     * Convert to response
     */
    private DisputeResolutionResponse convertToResponse(DisputeResolution resolution) {
        return DisputeResolutionResponse.builder()
                .id(resolution.getId())
                .disputeId(resolution.getDisputeId())
                .decision(resolution.getDecision())
                .decisionReason(resolution.getDecisionReason())
                .consequences(resolution.getConsequences())
                .actionType(resolution.getActionType())
                .appealed(resolution.getAppealed())
                .appealDaysRemaining(resolution.canAppeal() ? resolution.getAppealDaysRemaining() : 0)
                .resolvedAt(resolution.getResolvedAt())
                .publicSummary(resolution.getPublicSummary())
                .build();
    }
}
