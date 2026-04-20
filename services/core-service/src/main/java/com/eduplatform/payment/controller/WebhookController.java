package com.eduplatform.payment.controller;

import com.eduplatform.payment.service.PaymentWebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    @Autowired
    private PaymentWebhookService webhookService;

    @PostMapping("/razorpay")
    public ResponseEntity<?> handleRazorpayWebhook(@RequestBody String payload,
                                                   @RequestHeader("X-Razorpay-Signature") String signature) {
        try {
            webhookService.processWebhook(payload, signature);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
