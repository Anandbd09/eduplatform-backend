// FILE 24: PayoutSplitResponse.java
package com.eduplatform.referral.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PayoutSplitResponse {
    private Double totalAmount;
    private Double platformFee; // 5%
    private Double instructorAmount; // 95%
}