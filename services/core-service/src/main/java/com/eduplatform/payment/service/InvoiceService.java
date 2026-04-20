package com.eduplatform.payment.service;

import com.eduplatform.payment.model.*;
import com.eduplatform.payment.repository.InvoiceRepository;
import com.eduplatform.core.course.model.Course;
import com.eduplatform.core.course.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Slf4j
@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CourseRepository courseRepository;

    // Generate Invoice
    @Transactional
    public Invoice generateInvoice(Payment payment, Order order) {
        try {
            String invoiceNumber = generateInvoiceNumber();

            Course course = courseRepository.findById(order.getCourseId()).orElse(null);

            Invoice invoice = new Invoice();
            invoice.setId(UUID.randomUUID().toString());
            invoice.setInvoiceNumber(invoiceNumber);
            invoice.setUserId(payment.getUserId());
            invoice.setCourseId(order.getCourseId());
            invoice.setOrderId(order.getId());
            invoice.setPaymentId(payment.getId());

            invoice.setSubtotal(order.getSubtotal());
            invoice.setTaxAmount(order.getTax());
            invoice.setDiscountAmount(order.getDiscount());
            invoice.setTotalAmount(order.getTotalAmount());
            invoice.setCurrency("INR");

            invoice.setIssueDate(LocalDateTime.now());
            invoice.setDueDate(LocalDateTime.now().plusDays(30));
            invoice.setPaidDate(LocalDateTime.now());
            invoice.setStatus(InvoiceStatus.PAID);

            // Add line item
            InvoiceLineItem lineItem = new InvoiceLineItem();
            lineItem.setDescription("Course enrollment");
            lineItem.setCourseName(course != null ? course.getTitle() : "Course");
            lineItem.setQuantity(BigDecimal.ONE);
            lineItem.setUnitPrice(order.getSubtotal());
            lineItem.setAmount(order.getSubtotal());
            invoice.setLineItems(Arrays.asList(lineItem));

            invoice.setNotes("Thank you for your purchase!");
            invoice.setPaymentTerms("Payment received on " + LocalDateTime.now().toLocalDate());

            invoiceRepository.save(invoice);

            invoice.setPdfUrl("/api/v1/payments/invoices/" + invoice.getId() + "/download");
            invoiceRepository.save(invoice);

            log.info("Invoice generated: {}", invoiceNumber);

            return invoice;

        } catch (Exception e) {
            log.error("Error generating invoice", e);
            throw new RuntimeException("Failed to generate invoice");
        }
    }

    // Get Invoice
    public Invoice getInvoice(String invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
    }

    // Get User Invoices
    public List<Invoice> getUserInvoices(String userId) {
        return invoiceRepository.findByUserId(userId);
    }

    // Download Invoice
    public byte[] downloadInvoice(String invoiceId) {
        Invoice invoice = getInvoice(invoiceId);
        return buildPdfBytes(invoice);
    }

    private String buildInvoiceContent(Invoice invoice) {
        String lineItems = invoice.getLineItems() == null
                ? ""
                : invoice.getLineItems().stream()
                .map(item -> item.getDescription() + " - " + item.getAmount())
                .reduce((left, right) -> left + System.lineSeparator() + right)
                .orElse("");

        return String.join(System.lineSeparator(),
                "Invoice Number: " + invoice.getInvoiceNumber(),
                "User ID: " + invoice.getUserId(),
                "Course ID: " + invoice.getCourseId(),
                "Amount: " + invoice.getTotalAmount() + " " + invoice.getCurrency(),
                "Status: " + invoice.getStatus(),
                "Issue Date: " + invoice.getIssueDate(),
                "Line Items:",
                lineItems);
    }

    private byte[] buildPdfBytes(Invoice invoice) {
        String text = buildInvoiceContent(invoice)
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");

        String stream = "BT /F1 12 Tf 40 780 Td 14 TL (" +
                text.replace(System.lineSeparator(), ") Tj T* (") +
                ") Tj ET";

        StringBuilder pdf = new StringBuilder();
        List<Integer> offsets = new ArrayList<>();
        pdf.append("%PDF-1.4\n");

        offsets.add(pdf.length());
        pdf.append("1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n");

        offsets.add(pdf.length());
        pdf.append("2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n");

        offsets.add(pdf.length());
        pdf.append("3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] ")
                .append("/Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >> endobj\n");

        offsets.add(pdf.length());
        pdf.append("4 0 obj << /Length ").append(stream.getBytes(StandardCharsets.UTF_8).length)
                .append(" >> stream\n")
                .append(stream)
                .append("\nendstream endobj\n");

        offsets.add(pdf.length());
        pdf.append("5 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n");

        int xrefOffset = pdf.length();
        pdf.append("xref\n0 6\n");
        pdf.append("0000000000 65535 f \n");
        for (Integer offset : offsets) {
            pdf.append(String.format("%010d 00000 n %n", offset));
        }

        pdf.append("trailer << /Size 6 /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xrefOffset).append("\n%%EOF");
        return pdf.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String generateInvoiceNumber() {
        YearMonth now = YearMonth.now();
        String prefix = "INV-" + now.getYear() + "-" + String.format("%02d", now.getMonthValue());

        List<Invoice> invoices = invoiceRepository.findByIssueDateBetween(
                now.atDay(1).atStartOfDay(),
                now.atEndOfMonth().atTime(23, 59, 59)
        );

        return prefix + "-" + String.format("%05d", invoices.size() + 1);
    }
}
