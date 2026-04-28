// FILE 17: ReferralCodeRequest.java
package com.eduplatform.referral.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ReferralCodeRequest {
    private String instructorId;
    private String instructorName;
    private String instructorEmail;
}