// FILE 20: QRCodeResponse.java
package com.eduplatform.certificate.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class QRCodeResponse {
    private String qrCodeUrl;
    private String verificationUrl;
    private String certificateNumber;
}