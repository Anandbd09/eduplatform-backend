// FILE 19: RedemptionRequest.java
package com.eduplatform.coupon.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedemptionRequest {
    private String code;
    private String orderId;
    private Double originalAmount;
}