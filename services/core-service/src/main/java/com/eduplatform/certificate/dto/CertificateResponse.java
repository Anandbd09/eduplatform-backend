// FILE 18: CertificateResponse.java
package com.eduplatform.certificate.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CertificateResponse {
    private String id;
    private String certificateNumber;
    private String userId;
    private String userName;
    private String courseId;
    private String courseName;
    private String status;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private Long daysUntilExpiration;
    private Boolean isValid;
    private Integer downloadCount;
    private String qrCodeUrl;
    private String verificationUrl;
}