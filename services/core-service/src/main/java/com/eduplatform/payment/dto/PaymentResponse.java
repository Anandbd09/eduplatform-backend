package com.eduplatform.payment.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private String paymentId;
    private String orderId;
    private BigDecimal amount;
    private String status;
    private String paymentMethod;
    private String courseId;
    private LocalDateTime createdAt;
    private LocalDateTime capturedAt;
    private String receiptUrl;
    private String invoiceUrl;
}