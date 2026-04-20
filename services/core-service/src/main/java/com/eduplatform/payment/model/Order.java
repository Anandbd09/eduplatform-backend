package com.eduplatform.payment.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "orders")
public class Order {
    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String courseId;

    @Indexed
    private String razorpayOrderId;

    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal totalAmount;
    private String currency;

    @Indexed
    private OrderStatus status; // PENDING, PAID, FAILED, EXPIRED, CANCELLED

    private String couponCode;
    private BigDecimal discountAmount;

    // Line items
    private List<OrderItem> items;

    // Payment attempts
    private Integer paymentAttempts;
    private List<PaymentAttempt> attempts;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;

    // Metadata
    private String notes;
    private String receiptId;

    @Indexed
    private String tenantId;
}

@Data
class OrderItem {
    private String courseId;
    private String courseName;
    private BigDecimal price;
    private Integer quantity;
}

@Data
class PaymentAttempt {
    private String razorpayPaymentId;
    private LocalDateTime attemptedAt;
    private PaymentStatus status;
    private String errorCode;
    private String errorMessage;
}
