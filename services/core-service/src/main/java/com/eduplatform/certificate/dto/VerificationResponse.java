// FILE 19: VerificationResponse.java
package com.eduplatform.certificate.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VerificationResponse {
    private Boolean valid;
    private String status;
    private String message;
    private String holderName;
    private String courseName;
    private LocalDateTime issuedDate;
    private LocalDateTime expiryDate;
    private String issuerName;
    private Long daysUntilExpiration;
}