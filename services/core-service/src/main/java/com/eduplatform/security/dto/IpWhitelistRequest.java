// FILE 22: IpWhitelistRequest.java
package com.eduplatform.security.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class IpWhitelistRequest {
    private String userId;
    private String ipAddress;
    private String description;
}