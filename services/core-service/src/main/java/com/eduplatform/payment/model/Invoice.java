package com.eduplatform.payment.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "invoices")
public class Invoice {
    @Id
    private String id;

    @Indexed
    private String invoiceNumber; // INV-2024-001234

    @Indexed
    private String userId;
    private String courseId;

    @Indexed
    private String orderId;

    @Indexed
    private String paymentId;

    // Amounts
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String currency;

    // Dates
    private LocalDateTime issueDate;
    private LocalDateTime dueDate;
    private LocalDateTime paidDate;

    @Indexed
    private InvoiceStatus status; // DRAFT, ISSUED, PAID, OVERDUE, CANCELLED

    // Line items
    private List<InvoiceLineItem> lineItems;

    // Customer Details
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    // Company Details (from config)
    private String companyName;
    private String companyGST;
    private String companyAddress;

    // Notes
    private String notes;
    private String paymentTerms;

    // PDF
    private String pdfUrl;

    @Indexed
    private String tenantId;
}
