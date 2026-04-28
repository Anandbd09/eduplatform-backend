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
@RequestMapping("/api/v1/certificates")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    /**
     * ENDPOINT 1: Get user's certificates
     * GET /api/v1/certificates?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<?> getUserCertificates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            Page<CertificateResponse> certs = certificateService.getUserCertificates(userId, page, size, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Certificates retrieved", certs));
        } catch (Exception e) {
            log.error("Error fetching certificates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch certificates", null));
        }
    }

    /**
     * ENDPOINT 2: Get certificate by ID
     * GET /api/v1/certificates/{id}
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
     * ENDPOINT 3: Verify certificate by number (public endpoint)
     * GET /api/v1/certificates/verify/{certificateNumber}
     */
    @GetMapping("/verify/{certificateNumber}")
    public ResponseEntity<?> verifyCertificate(
            @PathVariable String certificateNumber,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            VerificationResponse response = certificateService.verifyCertificate(certificateNumber, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Verification completed", response));
        } catch (Exception e) {
            log.error("Error verifying certificate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Verification failed", null));
        }
    }

    /**
     * ENDPOINT 4: Get QR code
     * GET /api/v1/certificates/{id}/qr-code
     */
    @GetMapping("/{id}/qr-code")
    public ResponseEntity<?> getQRCode(
            @PathVariable String id,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            QRCodeResponse qrCode = certificateService.generateQRCode(id, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "QR code generated", qrCode));
        } catch (CertificateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * ENDPOINT 5: Download certificate
     * GET /api/v1/certificates/{id}/download
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadCertificate(
            @PathVariable String id,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            certificateService.trackDownload(id, tenantId);
            CertificateResponse cert = certificateService.getCertificateById(id, tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Download tracked", cert));
        } catch (Exception e) {
            log.error("Error downloading certificate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Download failed", null));
        }
    }

    /**
     * ENDPOINT 6: Get analytics
     * GET /api/v1/certificates/analytics
     */
    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        try {
            var analytics = certificateService.getCertificateAnalytics(tenantId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Analytics retrieved", analytics));
        } catch (Exception e) {
            log.error("Error fetching analytics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch analytics", null));
        }
    }

    /**
     * ENDPOINT 7: Health check
     * GET /api/v1/certificates/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Certificate service is healthy", null));
    }
}