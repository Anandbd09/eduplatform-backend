// FILE 23: CertificateAnalyticsResponse.java
package com.eduplatform.certificate.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CertificateAnalyticsResponse {
    private Long totalActive;
    private Long expired;
    private Long revoked;
    private Long expiringSoon;
}