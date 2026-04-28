package com.eduplatform.certificate.controller;

import com.eduplatform.certificate.service.CertificateService;
import com.eduplatform.certificate.dto.*;
import com.eduplatform.certificate.exception.CertificateException;
import com.eduplatform.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/certificates")
public class AdminCertificateController {

    @Autowired
    private CertificateService certificateService;

    /**
     * ENDPOINT 8: Issue certificate (ADMIN)
     * POST /api/v1/admin/certificates
     */
    @PostMapping
    public ResponseEntity<?> issueCertificate(
            @RequestBody CertificateRequest request,
            @RequestHeader("X-User-Id") String adminId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            CertificateResponse cert = certificateService.issueCertificate(request, adminId, tenantId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Certificate issued", cert));
        } catch (CertificateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * ENDPOINT 9: Get all certificates (ADMIN)
     * GET /api/v1/admin/certificates?page=0&size=10&status=ACTIVE
     */
    @GetMapping
    public ResponseEntity<?> getAllCertificates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            Page<CertificateResponse> certs = certificateService.getAllCertificates(page, size, status, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Certificates retrieved", certs));
        } catch (Exception e) {
            log.error("Error fetching certificates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch certificates", null));
        }
    }

    /**
     * ENDPOINT 10: Get certificate by ID (ADMIN)
     * GET /api/v1/admin/certificates/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCertificateById(
            @PathVariable String id,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            CertificateResponse cert = certificateService.getCertificateById(id, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Certificate retrieved", cert));
        } catch (CertificateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * ENDPOINT 11: Revoke certificate (ADMIN)
     * POST /api/v1/admin/certificates/{id}/revoke
     */
    @PostMapping("/{id}/revoke")
    public ResponseEntity<?> revokeCertificate(
            @PathVariable String id,
            @RequestParam String reason,
            @RequestParam(required = false) String details,
            @RequestHeader("X-User-Id") String adminId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            certificateService.revokeCertificate(id, reason, details, adminId, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Certificate revoked", null));
        } catch (CertificateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * ENDPOINT 12: Create template (ADMIN)
     * POST /api/v1/admin/certificates/templates
     */
    @PostMapping("/templates")
    public ResponseEntity<?> createTemplate(
            @RequestBody TemplateRequest request,
            @RequestHeader("X-User-Id") String adminId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            TemplateResponse template = certificateService.createTemplate(request, adminId, tenantId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Template created", template));
        } catch (CertificateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * ENDPOINT 13: Get templates (ADMIN)
     * GET /api/v1/admin/certificates/templates
     */
    @GetMapping("/templates")
    public ResponseEntity<?> getTemplates(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            var templates = certificateService.getTemplates(tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Templates retrieved", templates));
        } catch (Exception e) {
            log.error("Error fetching templates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch templates", null));
        }
    }

    /**
     * ENDPOINT 14: Health check
     * GET /api/v1/admin/certificates/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Certificate service is healthy", null));
    }
}