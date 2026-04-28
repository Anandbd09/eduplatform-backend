// FILE 21: ValidationResponse.java
package com.eduplatform.coupon.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationResponse {
    private Boolean valid;
    private String message;
    private String discountType;
    private Double discountValue;
    private Double maxDiscount;
    private Double discountAmount;
    private Double finalAmount;
}