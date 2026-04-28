// FILE 24: PromotionResponse.java
package com.eduplatform.coupon.dto;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionResponse {
    private String id;
    private String campaignCode;
    private String name;
    private String description;
    private String type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private List<String> couponIds;
    private String targetAudience;
    private Long expectedReach;
    private Long actualReach;
    private Double budgetAllocated;
    private Double budgetUsed;
}