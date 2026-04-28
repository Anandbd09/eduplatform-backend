package com.eduplatform.certificate.service;

import com.eduplatform.certificate.model.*;
import com.eduplatform.certificate.repository.*;
import com.eduplatform.certificate.dto.*;
import com.eduplatform.certificate.util.CertificateValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class GenerationService {

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private CertificateTemplateRepository templateRepository;

    /**
     * GENERATE CERTIFICATE
     */
    public Certificate generateCertificate(CertificateRequest request, String adminId, String tenantId) {
        try {
            // Validate request
            CertificateValidator.validateCertificateRequest(request);

            // Generate certificate number
            String certNumber = generateCertificateNumber();

            // Calculate expiry (2 years from now)
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = now.plusYears(2);

            // Create certificate
            Certificate certificate = Certificate.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(request.getUserId())
                    .userName(request.getUserName())
                    .userEmail(request.getUserEmail())
                    .courseId(request.getCourseId())
                    .courseName(request.getCourseName())
                    .certificateNumber(certNumber)
                    .templateId(request.getTemplateId())
                    .issuedAt(now)
                    .expiresAt(expiresAt)
                    .status("ACTIVE")
                    .issuerName("EduPlatform")
                    .issuerSignature(adminId)
                    .customFields(request.getCustomFields())
                    .courseCompletion(request.getCourseCompletion())
                    .totalLessons(request.getTotalLessons())
                    .completedLessons(request.getCompletedLessons())
                    .downloadCount(0)
                    .verificationUrl("/api/v1/certificates/verify/" + certNumber)
                    .createdAt(now)
                    .updatedAt(now)
                    .tenantId(tenantId)
                    .build();

            Certificate saved = certificateRepository.save(certificate);
            log.info("Certificate generated: certNum={}, userId={}", certNumber, request.getUserId());
            return saved;

        } catch (Exception e) {
            log.error("Error generating certificate", e);
            throw new RuntimeException("Failed to generate certificate: " + e.getMessage());
        }
    }

    /**
     * GENERATE UNIQUE CERTIFICATE NUMBER
     */
    private String generateCertificateNumber() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "CERT-" + year + "-" + random;
    }
}