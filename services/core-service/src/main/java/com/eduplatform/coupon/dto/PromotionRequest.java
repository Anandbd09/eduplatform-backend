// FILE 23: PromotionRequest.java
package com.eduplatform.coupon.dto;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionRequest {
    private String campaignCode;
    private String name;
    private String description;
    private String type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<String> couponIds;
    private String targetAudience;
    private Double budgetAllocated;
    private String promotionChannel;
}