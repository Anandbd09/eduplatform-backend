package com.eduplatform.payment.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderResponse {
    private String orderId;
    private String razorpayOrderId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime expiresAt;
    private String shortUrl; // Razorpay payment link
}