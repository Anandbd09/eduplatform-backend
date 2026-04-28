package com.eduplatform.certificate.service;

import com.eduplatform.certificate.model.Certificate;
import com.eduplatform.certificate.dto.QRCodeResponse;
import com.eduplatform.certificate.util.QRCodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class QRCodeService {

    /**
     * GENERATE QR CODE FOR CERTIFICATE
     */
    public QRCodeResponse generateQRCode(Certificate certificate) {
        try {
            // Generate verification URL
            String verificationUrl = buildVerificationUrl(certificate);

            // Generate QR code
            String qrCodeUrl = QRCodeGenerator.generateQRCode(verificationUrl, 200, 200);

            log.info("QR code generated for certificate: {}", certificate.getCertificateNumber());

            return QRCodeResponse.builder()
                    .qrCodeUrl(qrCodeUrl)
                    .verificationUrl(verificationUrl)
                    .certificateNumber(certificate.getCertificateNumber())
                    .build();

        } catch (Exception e) {
            log.error("Error generating QR code", e);
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage());
        }
    }

    /**
     * BUILD VERIFICATION URL
     */
    private String buildVerificationUrl(Certificate certificate) {
        return "https://eduplatform.com/verify/" + certificate.getCertificateNumber();
    }
}