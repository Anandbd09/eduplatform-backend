package com.eduplatform.referral.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "referral_payouts")
public class ReferralPayout {

    @Id
    private String id;

    @Indexed
    private String instructorId;

    private String instructorName;

    private String instructorEmail;

    @Indexed
    private String status; // PENDING, APPROVED, PAID, FAILED, REJECTED

    private List<String> rewardIds; // Which rewards included in this payout

    private Long rewardCount;

    private Double totalAmount; // Sum of all rewards

    private Double platformFee; // 5% fee (if applicable)

    private Double netAmount; // totalAmount - platformFee

    @Indexed
    private LocalDateTime requestedAt;

    @Indexed
    private LocalDateTime approvedAt;

    @Indexed
    private LocalDateTime paidAt;

    private String bankAccountId; // Instructor's linked bank account

    private String paymentMethod; // BANK_TRANSFER, UPI, CRYPTO

    private String transactionId; // From Razorpay/payment processor

    private String failureReason; // If payment failed

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;
}