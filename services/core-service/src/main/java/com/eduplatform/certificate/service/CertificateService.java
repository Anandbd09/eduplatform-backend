package com.eduplatform.certificate.service;

import com.eduplatform.certificate.model.*;
import com.eduplatform.certificate.repository.*;
import com.eduplatform.certificate.dto.*;
import com.eduplatform.certificate.exception.CertificateException;
import com.eduplatform.certificate.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class CertificateService {

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private CertificateTemplateRepository templateRepository;

    @Autowired
    private CertificateVerificationRepository verificationRepository;

    @Autowired
    private CertificateRevocationRepository revocationRepository;

    @Autowired
    private CertificateBadgeRepository badgeRepository;

    @Autowired
    private GenerationService generationService;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private QRCodeService qrCodeService;

    /**
     * GENERATE & ISSUE CERTIFICATE (ADMIN)
     */
    public CertificateResponse issueCertificate(CertificateRequest request, String adminId, String tenantId) {
        try {
            // Check if certificate already exists
            Optional<Certificate> existing = certificateRepository
                    .findByUserIdAndCourseIdAndTenantId(request.getUserId(), request.getCourseId(), tenantId);

            if (existing.isPresent() && "ACTIVE".equals(existing.get().getStatus())) {
                throw new CertificateException("Active certificate already exists for this user + course combo");
            }

            // Generate certificate
            Certificate certificate = generationService.generateCertificate(request, adminId, tenantId);

            log.info("Certificate issued: certNum={}, userId={}, courseId={}",
                    certificate.getCertificateNumber(), request.getUserId(), request.getCourseId());

            return convertToResponse(certificate);

        } catch (CertificateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error issuing certificate", e);
            throw new CertificateException("Failed to issue certificate: " + e.getMessage());
        }
    }

    /**
     * GET USER'S CERTIFICATES
     */
    public Page<CertificateResponse> getUserCertificates(String userId, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("issuedAt").descending());

            Page<Certificate> certs = certificateRepository.findByUserIdAndTenantId(userId, tenantId, pageable);
            return certs.map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error fetching user certificates", e);
            throw new CertificateException("Failed to fetch certificates");
        }
    }

    /**
     * GET CERTIFICATE BY ID
     */
    public CertificateResponse getCertificateById(String id, String tenantId) {
        try {
            Optional<Certificate> cert = certificateRepository.findById(id);
            if (cert.isPresent() && cert.get().getTenantId().equals(tenantId)) {
                return convertToResponse(cert.get());
            }
            throw new CertificateException("Certificate not found");
        } catch (Exception e) {
            log.error("Error fetching certificate", e);
            throw new CertificateException("Failed to fetch certificate");
        }
    }

    /**
     * VERIFY CERTIFICATE BY NUMBER (PUBLIC ENDPOINT)
     */
    public VerificationResponse verifyCertificate(String certificateNumber, String tenantId) {
        try {
            Optional<Certificate> cert = certificateRepository
                    .findByCertificateNumberAndTenantId(certificateNumber, tenantId);

            if (cert.isEmpty()) {
                return VerificationResponse.builder()
                        .valid(false)
                        .status("INVALID")
                        .message("Certificate not found")
                        .build();
            }

            Certificate certificate = cert.get();
            return verificationService.verifyCertificate(certificate);

        } catch (Exception e) {
            log.error("Error verifying certificate", e);
            return VerificationResponse.builder()
                    .valid(false)
                    .status("ERROR")
                    .message("Verification failed: " + e.getMessage())
                    .build();
        }
    }

    /**
     * GET QR CODE FOR CERTIFICATE
     */
    public QRCodeResponse generateQRCode(String certificateId, String tenantId) {
        try {
            Optional<Certificate> cert = certificateRepository.findById(certificateId);
            if (cert.isEmpty() || !cert.get().getTenantId().equals(tenantId)) {
                throw new CertificateException("Certificate not found");
            }

            return qrCodeService.generateQRCode(cert.get());

        } catch (CertificateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error generating QR code", e);
            throw new CertificateException("Failed to generate QR code");
        }
    }

    /**
     * DOWNLOAD CERTIFICATE (TRACK DOWNLOADS)
     */
    public void trackDownload(String certificateId, String tenantId) {
        try {
            Optional<Certificate> cert = certificateRepository.findById(certificateId);
            if (cert.isPresent() && cert.get().getTenantId().equals(tenantId)) {
                Certificate certificate = cert.get();
                certificate.setDownloadCount((certificate.getDownloadCount() != null ?
                        certificate.getDownloadCount() : 0) + 1);
                certificate.setDownloadedAt(LocalDateTime.now());
                certificateRepository.save(certificate);
                log.info("Certificate download tracked: id={}", certificateId);
            }
        } catch (Exception e) {
            log.error("Error tracking download", e);
        }
    }

    /**
     * REVOKE CERTIFICATE
     */
    public void revokeCertificate(String certificateId, String reason, String details,
                                  String adminId, String tenantId) {
        try {
            Optional<Certificate> cert = certificateRepository.findById(certificateId);
            if (cert.isEmpty() || !cert.get().getTenantId().equals(tenantId)) {
                throw new CertificateException("Certificate not found");
            }

            Certificate certificate = cert.get();

            // Update certificate status
            certificate.setStatus("REVOKED");
            certificate.setRevocationReason(reason);
            certificate.setRevokedAt(LocalDateTime.now());
            certificate.setRevokedBy(adminId);
            certificateRepository.save(certificate);

            // Create revocation record
            CertificateRevocation revocation = CertificateRevocation.builder()
                    .id(UUID.randomUUID().toString())
                    .certificateId(certificateId)
                    .certificateNumber(certificate.getCertificateNumber())
                    .userId(certificate.getUserId())
                    .courseId(certificate.getCourseId())
                    .reason(reason)
                    .details(details)
                    .revokedAt(LocalDateTime.now())
                    .revokedBy(adminId)
                    .status("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();
            revocationRepository.save(revocation);

            log.info("Certificate revoked: id={}, reason={}, by={}", certificateId, reason, adminId);

        } catch (CertificateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error revoking certificate", e);
            throw new CertificateException("Failed to revoke certificate");
        }
    }

    /**
     * GET ALL CERTIFICATES (ADMIN)
     */
    public Page<CertificateResponse> getAllCertificates(int page, int size, String status, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("issuedAt").descending());

            Page<Certificate> certs;
            if (status != null && !status.isEmpty()) {
                certs = certificateRepository.findByStatusAndTenantId(status, tenantId, pageable);
            } else {
                certs = certificateRepository.findAll(pageable);
            }

            return certs.map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error fetching certificates", e);
            throw new CertificateException("Failed to fetch certificates");
        }
    }

    /**
     * CREATE TEMPLATE
     */
    public TemplateResponse createTemplate(TemplateRequest request, String adminId, String tenantId) {
        try {
            // Check duplicate
            Optional<CertificateTemplate> existing = templateRepository
                    .findByNameAndTenantId(request.getName(), tenantId);
            if (existing.isPresent()) {
                throw new CertificateException("Template with this name already exists");
            }

            CertificateTemplate template = CertificateTemplate.builder()
                    .id(UUID.randomUUID().toString())
                    .name(request.getName())
                    .description(request.getDescription())
                    .templateDesignUrl(request.getTemplateDesignUrl())
                    .status("ACTIVE")
                    .borderDesign(request.getBorderDesign())
                    .backgroundColor(request.getBackgroundColor())
                    .fontFamily(request.getFontFamily())
                    .signature(request.getSignature())
                    .seal(request.getSeal())
                    .customFieldNames(request.getCustomFieldNames())
                    .language(request.getLanguage())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .createdBy(adminId)
                    .tenantId(tenantId)
                    .build();

            CertificateTemplate saved = templateRepository.save(template);
            log.info("Template created: id={}, name={}", saved.getId(), saved.getName());
            return convertTemplateToResponse(saved);

        } catch (CertificateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating template", e);
            throw new CertificateException("Failed to create template");
        }
    }

    /**
     * GET TEMPLATES
     */
    public List<TemplateResponse> getTemplates(String tenantId) {
        try {
            List<CertificateTemplate> templates = templateRepository.findByStatusAndTenantId("ACTIVE", tenantId);
            return templates.stream()
                    .map(this::convertTemplateToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching templates", e);
            throw new CertificateException("Failed to fetch templates");
        }
    }

    /**
     * GET ANALYTICS
     */
    public Map<String, Object> getCertificateAnalytics(String tenantId) {
        try {
            Long totalCerts = certificateRepository.countByStatusAndTenantId("ACTIVE", tenantId);
            Long expiredCount = certificateRepository.countByStatusAndTenantId("EXPIRED", tenantId);
            Long revokedCount = certificateRepository.countByStatusAndTenantId("REVOKED", tenantId);

            List<Certificate> expiringSoon = certificateRepository
                    .findByExpiresAtBeforeAndStatusAndTenantId(
                            LocalDateTime.now().plusDays(30), "ACTIVE", tenantId);

            return Map.of(
                    "totalActive", totalCerts,
                    "expired", expiredCount,
                    "revoked", revokedCount,
                    "expiringSoon", (long) expiringSoon.size(),
                    "lastUpdated", LocalDateTime.now()
            );

        } catch (Exception e) {
            log.error("Error getting analytics", e);
            throw new CertificateException("Failed to get analytics");
        }
    }

    /**
     * CONVERT TO RESPONSE
     */
    private CertificateResponse convertToResponse(Certificate cert) {
        return CertificateResponse.builder()
                .id(cert.getId())
                .certificateNumber(cert.getCertificateNumber())
                .userId(cert.getUserId())
                .userName(cert.getUserName())
                .courseId(cert.getCourseId())
                .courseName(cert.getCourseName())
                .status(cert.getStatus())
                .issuedAt(cert.getIssuedAt())
                .expiresAt(cert.getExpiresAt())
                .daysUntilExpiration(cert.getDaysUntilExpiration())
                .isValid(cert.isValid())
                .downloadCount(cert.getDownloadCount())
                .qrCodeUrl(cert.getQrCodeUrl())
                .verificationUrl(cert.getVerificationUrl())
                .build();
    }

    /**
     * CONVERT TEMPLATE TO RESPONSE
     */
    private TemplateResponse convertTemplateToResponse(CertificateTemplate template) {
        return TemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .status(template.getStatus())
                .language(template.getLanguage())
                .build();
    }
}