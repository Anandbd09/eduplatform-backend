package com.eduplatform.payment.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(collection = "payments")
public class Payment {
    @Id
    private String id;

    @Indexed
    private String userId;
    private String courseId;

    @Indexed
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    private BigDecimal amount;
    private String currency;

    @Indexed
    private PaymentStatus status; // CREATED, AUTHORIZED, CAPTURED, FAILED, REFUNDED

    private PaymentMethod paymentMethod; // CARD, UPI, NETBANKING, WALLET
    private String paymentGateway; // RAZORPAY

    // Card Details (encrypted)
    private String cardLast4;
    private String cardIssuer;

    // UPI Details
    private String upiVpa;

    // Bank Details
    private String bankName;
    private String bankAccountLast4;

    // Metadata
    private String description;
    private String receiptId;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime authorizedAt;
    private LocalDateTime capturedAt;
    private LocalDateTime failedAt;
    private LocalDateTime refundedAt;

    // Error Details
    private String errorCode;
    private String errorDescription;

    // Refund Details
    private BigDecimal refundAmount;
    private String refundReason;
    private LocalDateTime refundRequestedAt;

    @Indexed
    private String tenantId;
}
