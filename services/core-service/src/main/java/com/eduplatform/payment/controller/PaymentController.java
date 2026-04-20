package com.eduplatform.payment.controller;

import com.eduplatform.payment.dto.*;
import com.eduplatform.payment.service.*;
import com.eduplatform.core.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private InvoiceService invoiceService;

    // Create Order
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request,
                                         @RequestHeader("X-User-Id") String userId) {
        try {
            OrderResponse response = paymentService.createOrder(request, userId);
            return ResponseEntity.ok(ApiResponse.success(response, "Order created successfully"));
        } catch (Exception e) {
            log.error("Error creating order", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "PAYMENT_ORDER_CREATE_FAILED"));
        }
    }

    // Verify Payment
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody VerifyPaymentRequest request,
                                           @RequestHeader("X-User-Id") String userId) {
        try {
            PaymentResponse response = paymentService.verifyPayment(request, userId);
            return ResponseEntity.ok(ApiResponse.success(response, "Payment verified successfully"));
        } catch (Exception e) {
            log.error("Error verifying payment", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "PAYMENT_VERIFY_FAILED"));
        }
    }

    // Get Payment History
    @GetMapping("/history")
    public ResponseEntity<?> getPaymentHistory(@RequestHeader("X-User-Id") String userId) {
        try {
            var response = paymentService.getPaymentHistory(userId);
            return ResponseEntity.ok(ApiResponse.success(response, "Payment history retrieved"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "PAYMENT_HISTORY_FAILED"));
        }
    }

    // Refund Payment
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<?> refundPayment(@PathVariable String paymentId,
                                           @RequestParam BigDecimal amount,
                                           @RequestParam String reason) {
        try {
            paymentService.refundPayment(paymentId, amount, reason);
            return ResponseEntity.ok(ApiResponse.success(null, "Refund processed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "PAYMENT_REFUND_FAILED"));
        }
    }

    // Get Subscriptions
    @GetMapping("/subscriptions")
    public ResponseEntity<?> getSubscriptions(@RequestHeader("X-User-Id") String userId) {
        try {
            var response = subscriptionService.getAllSubscriptions(userId);
            return ResponseEntity.ok(ApiResponse.success(response, "Subscriptions retrieved"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "SUBSCRIPTIONS_FETCH_FAILED"));
        }
    }

    // Cancel Subscription
    @PostMapping("/subscriptions/{subscriptionId}/cancel")
    public ResponseEntity<?> cancelSubscription(@PathVariable String subscriptionId,
                                                @RequestParam String reason) {
        try {
            subscriptionService.cancelSubscription(subscriptionId, reason);
            return ResponseEntity.ok(ApiResponse.success(null, "Subscription cancelled"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "SUBSCRIPTION_CANCEL_FAILED"));
        }
    }

    // Download Invoice
    @GetMapping("/invoices/{invoiceId}/download")
    public ResponseEntity<?> downloadInvoice(@PathVariable String invoiceId) {
        try {
            byte[] pdf = invoiceService.downloadInvoice(invoiceId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + invoiceId + ".pdf")
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "INVOICE_DOWNLOAD_FAILED"));
        }
    }
}
