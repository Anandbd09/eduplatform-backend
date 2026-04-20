package com.eduplatform.payment.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceLineItem {
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String courseName;
}
