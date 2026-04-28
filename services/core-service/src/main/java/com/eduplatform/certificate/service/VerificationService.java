package com.eduplatform.certificate.service;

import com.eduplatform.certificate.model.*;
import com.eduplatform.certificate.repository.*;
import com.eduplatform.certificate.dto.VerificationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class VerificationService {

    @Autowired
    private CertificateVerificationRepository verificationRepository;

    @Autowired
    private CertificateRevocationRepository revocationRepository;

    /**
     * VERIFY CERTIFICATE
     */
    public VerificationResponse verifyCertificate(Certificate certificate) {
        try {
            // Check if revoked
            if ("REVOKED".equals(certificate.getStatus())) {
                logVerification(certificate, "REVOKED");
                return VerificationResponse.builder()
                        .valid(false)
                        .status("REVOKED")
                        .message("Certificate has been revoked")
                        .holderName(certificate.getUserName())
                        .courseName(certificate.getCourseName())
                        .issuedDate(certificate.getIssuedAt())
                        .expiryDate(certificate.getExpiresAt())
                        .build();
            }

            // Check if expired
            if (certificate.isExpired()) {
                certificate.setStatus("EXPIRED");
                logVerification(certificate, "EXPIRED");
                return VerificationResponse.builder()
                        .valid(false)
                        .status("EXPIRED")
                        .message("Certificate has expired")
                        .holderName(certificate.getUserName())
                        .courseName(certificate.getCourseName())
                        .issuedDate(certificate.getIssuedAt())
                        .expiryDate(certificate.getExpiresAt())
                        .build();
            }

            // Valid certificate
            logVerification(certificate, "VALID");
            return VerificationResponse.builder()
                    .valid(true)
                    .status("VALID")
                    .message("Certificate is valid")
                    .holderName(certificate.getUserName())
                    .courseName(certificate.getCourseName())
                    .issuedDate(certificate.getIssuedAt())
                    .expiryDate(certificate.getExpiresAt())
                    .issuerName("EduPlatform")
                    .daysUntilExpiration(certificate.getDaysUntilExpiration())
                    .build();

        } catch (Exception e) {
            log.error("Error verifying certificate", e);
            return VerificationResponse.builder()
                    .valid(false)
                    .status("ERROR")
                    .message("Verification error: " + e.getMessage())
                    .build();
        }
    }

    /**
     * LOG VERIFICATION ATTEMPT
     */
    private void logVerification(Certificate certificate, String status) {
        try {
            CertificateVerification verification = CertificateVerification.builder()
                    .id(UUID.randomUUID().toString())
                    .certificateNumber(certificate.getCertificateNumber())
                    .certificateId(certificate.getId())
                    .holderName(certificate.getUserName())
                    .courseName(certificate.getCourseName())
                    .issuedDate(certificate.getIssuedAt())
                    .expiryDate(certificate.getExpiresAt())
                    .issuerName("EduPlatform")
                    .verificationStatus(status)
                    .verifiedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .tenantId(certificate.getTenantId())
                    .build();

            verificationRepository.save(verification);
            log.debug("Verification logged: certNum={}, status={}", certificate.getCertificateNumber(), status);
        } catch (Exception e) {
            log.error("Error logging verification", e);
        }
    }
}