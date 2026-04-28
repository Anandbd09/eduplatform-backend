package com.eduplatform.certificate.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "certificate_verifications")
public class CertificateVerification {

    @Id
    private String id;

    @Indexed(unique = true)
    private String certificateNumber;

    @Indexed
    private String certificateId;

    private String holderName;

    private String courseName;

    @Indexed
    private LocalDateTime issuedDate;

    @Indexed
    private LocalDateTime expiryDate;

    private String issuerName;

    @Indexed
    private String verificationStatus; // VALID, INVALID, EXPIRED, REVOKED

    @Indexed
    private LocalDateTime verifiedAt;

    @Indexed
    private LocalDateTime createdAt;

    private String verifierIpAddress;

    @Indexed
    private String tenantId;

    private Long version = 0L;
}