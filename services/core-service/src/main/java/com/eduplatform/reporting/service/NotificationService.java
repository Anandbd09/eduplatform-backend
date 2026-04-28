package com.eduplatform.reporting.service;

import com.eduplatform.reporting.model.*;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service("reportingNotificationService")
public class NotificationService {

    /**
     * Notify when report is created
     */
    public void notifyReportCreated(Report report) {
        try {
            log.info("✉️ Report created notification: Reporter: {}, Category: {}",
                    report.getReporterId(), report.getCategory());

            // Send email to reporter
            String subject = "Report Submission Confirmation";
            String body = "Your report has been submitted successfully. Report ID: " + report.getId() +
                    "\nCategory: " + report.getCategory() +
                    "\nStatus: " + report.getStatus() +
                    "\nWe will review your report and take appropriate action.";

            // Call email service (Resend integration)
            sendEmail(report.getReporterEmail(), subject, body);

        } catch (Exception e) {
            log.warn("Failed to send report creation notification", e);
        }
    }

    /**
     * Notify when report status changes
     */
    public void notifyReportStatusChanged(Report report) {
        try {
            log.info("✉️ Report status changed: Report ID: {}, New Status: {}",
                    report.getId(), report.getStatus());

            String subject = "Report Status Update";
            String body = "Your report (ID: " + report.getId() + ") status has been updated.\n" +
                    "New Status: " + report.getStatus();

            sendEmail(report.getReporterEmail(), subject, body);

        } catch (Exception e) {
            log.warn("Failed to send status change notification", e);
        }
    }

    /**
     * Notify when dispute is created
     */
    public void notifyDisputeCreated(Dispute dispute) {
        try {
            log.info("✉️ Dispute created notification: Disputed User: {}", dispute.getDisputedUserId());

            String subject = "Formal Dispute Filed Against You";
            String body = "A formal dispute has been filed against you regarding: " + dispute.getReason() +
                    "\nDispute ID: " + dispute.getId() +
                    "\nYou have 7 days to respond to this dispute.";

            sendEmail(dispute.getDisputedUserEmail(), subject, body);

        } catch (Exception e) {
            log.warn("Failed to send dispute creation notification", e);
        }
    }

    /**
     * Notify when dispute is assigned
     */
    public void notifyDisputeAssigned(Dispute dispute) {
        try {
            log.info("✉️ Dispute assigned notification: Dispute ID: {}, Admin: {}",
                    dispute.getId(), dispute.getAssignedTo());

            String subject = "Your Dispute Has Been Assigned for Review";
            String body = "Your dispute has been assigned to a reviewer.\n" +
                    "Dispute ID: " + dispute.getId() +
                    "\nReviewer: " + dispute.getAssignedToName() +
                    "\nResponse Deadline: " + dispute.getResponseDeadline().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            sendEmail(dispute.getDisputedUserEmail(), subject, body);

        } catch (Exception e) {
            log.warn("Failed to send assignment notification", e);
        }
    }

    /**
     * Notify when user submits response
     */
    public void notifyUserResponseReceived(Dispute dispute) {
        try {
            log.info("✉️ User response received: Dispute ID: {}", dispute.getId());

            String subject = "Response Received for Dispute #" + dispute.getId();
            String body = "We have received your response to the dispute.\n" +
                    "Our team will review your response and the original complaint.\n" +
                    "You will be notified of the resolution within 5 business days.";

            sendEmail(dispute.getDisputedUserEmail(), subject, body);

        } catch (Exception e) {
            log.warn("Failed to send response confirmation notification", e);
        }
    }

    /**
     * Notify when dispute is resolved
     */
    public void notifyDisputeResolved(Dispute dispute, DisputeResolution resolution) {
        try {
            log.info("✉️ Dispute resolved notification: Dispute ID: {}, Decision: {}",
                    dispute.getId(), resolution.getDecision());

            String subject = "Dispute Resolution: " + resolution.getDecision();
            String body = "The dispute has been resolved.\n" +
                    "Decision: " + resolution.getDecision() +
                    "\nReason: " + resolution.getDecisionReason() +
                    "\nConsequences: " + resolution.getConsequences() +
                    "\nYou have 7 days to file an appeal if you wish to contest this decision.";

            sendEmail(dispute.getDisputedUserEmail(), subject, body);

        } catch (Exception e) {
            log.warn("Failed to send resolution notification", e);
        }
    }

    /**
     * Notify when appeal is submitted
     */
    public void notifyAppealSubmitted(DisputeResolution resolution) {
        try {
            log.info("✉️ Appeal submitted notification: Dispute ID: {}", resolution.getDisputeId());

            String subject = "Appeal Submitted - Dispute #" + resolution.getDisputeId();
            String body = "An appeal has been submitted for dispute #" + resolution.getDisputeId() +
                    "\nAppeal Reason: " + resolution.getAppealReason() +
                    "\nOur appeals team will review this within 3 business days.";

            // Notify admins
            log.info("Admin team notified of appeal");

        } catch (Exception e) {
            log.warn("Failed to send appeal notification", e);
        }
    }

    /**
     * Send email via service
     */
    private void sendEmail(String to, String subject, String body) {
        // Integration with Resend email service
        // This is a placeholder - actual implementation would use EmailService
        log.debug("Email queued: To={}, Subject={}", to, subject);
    }
}
