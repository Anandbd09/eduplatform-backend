package com.eduplatform.payment.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateOrderRequest {
    private String courseId;
    private BigDecimal amount;
    private String currency; // INR
    private String paymentMethod; // CARD, UPI, NETBANKING, WALLET
    private String couponCode; // Optional
    private String notes; // Optional
}