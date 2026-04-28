// FILE 23: IpWhitelistResponse.java
package com.eduplatform.security.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class IpWhitelistResponse {
    private String ipAddress;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}