// FILE 20: RedemptionResponse.java
package com.eduplatform.coupon.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedemptionResponse {
    private String id;
    private String code;
    private String orderId;
    private Double originalAmount;
    private Double discountAmount;
    private Double finalAmount;
    private String status;
    private LocalDateTime redeemedAt;
}