package com.eduplatform.admin.service;

import com.eduplatform.admin.model.AuditAction;
import com.eduplatform.admin.model.DisputeResolution;
import com.eduplatform.admin.model.DisputeStatus;
import com.eduplatform.admin.model.PaymentDispute;
import com.eduplatform.admin.repository.PaymentDisputeRepository;
import com.eduplatform.payment.model.Payment;
import com.eduplatform.payment.model.PaymentStatus;
import com.eduplatform.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class PaymentManagementService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentDisputeRepository disputeRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public void issueRefund(String adminId, String paymentId, BigDecimal refundAmount, String reason) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            if (payment.getStatus() != PaymentStatus.CAPTURED) {
                throw new RuntimeException("Payment is not eligible for refund");
            }

            BigDecimal refundValue = refundAmount == null ? BigDecimal.ZERO : refundAmount;
            payment.setStatus(refundValue.compareTo(payment.getAmount()) >= 0
                    ? PaymentStatus.REFUNDED
                    : PaymentStatus.PARTIALLY_REFUNDED);
            payment.setRefundAmount(refundAmount);
            payment.setRefundReason(reason);
            payment.setRefundRequestedAt(LocalDateTime.now());
            payment.setRefundedAt(LocalDateTime.now());

            paymentRepository.save(payment);

            auditLogService.logAction(
                    adminId,
                    AuditAction.PAYMENT_REFUNDED,
                    paymentId,
                    "PAYMENT",
                    "Refund issued: INR " + refundAmount + " - " + reason
            );

            log.info("Refund issued: {} - Amount: {}", paymentId, refundAmount);
        } catch (Exception e) {
            log.error("Error issuing refund", e);
            throw new RuntimeException("Failed to issue refund");
        }
    }

    @Transactional
    public void resolveDispute(String adminId, String disputeId, String resolution,
                               BigDecimal refundAmount, String notes) {
        try {
            PaymentDispute dispute = disputeRepository.findById(disputeId)
                    .orElseThrow(() -> new RuntimeException("Dispute not found"));

            DisputeResolution disputeResolution = DisputeResolution.valueOf(normalizeValue(resolution));

            dispute.setStatus(DisputeStatus.RESOLVED);
            dispute.setResolution(disputeResolution);
            dispute.setRefundAmount(refundAmount);
            dispute.setResolutionNotes(notes);
            dispute.setResolvedBy(adminId);
            dispute.setResolvedAt(LocalDateTime.now());

            disputeRepository.save(dispute);

            if (disputeResolution == DisputeResolution.FULL_REFUND) {
                issueRefund(adminId, dispute.getPaymentId(), dispute.getAmount(),
                        "Dispute resolved with full refund");
            } else if (disputeResolution == DisputeResolution.PARTIAL_REFUND) {
                issueRefund(adminId, dispute.getPaymentId(), refundAmount,
                        "Dispute resolved with partial refund");
            }

            auditLogService.logAction(
                    adminId,
                    AuditAction.DISPUTE_RESOLVED,
                    disputeId,
                    "DISPUTE",
                    "Dispute resolved: " + disputeResolution
            );
        } catch (Exception e) {
            log.error("Error resolving dispute", e);
            throw new RuntimeException("Failed to resolve dispute");
        }
    }

    public List<PaymentDispute> getOpenDisputes() {
        return disputeRepository.findByStatusOrderByCreatedAtDesc(DisputeStatus.OPEN.name());
    }

    private String normalizeValue(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
