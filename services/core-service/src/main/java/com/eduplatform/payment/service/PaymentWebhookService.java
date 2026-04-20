package com.eduplatform.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentWebhookService {

    @Autowired
    private RazorpayService razorpayService;

    public void processWebhook(String payload, String signature) {
        if (!razorpayService.verifyWebhookSignature(payload, signature)) {
            throw new IllegalArgumentException("Invalid webhook signature");
        }

        JSONObject event = new JSONObject(payload);
        String eventType = event.optString("event", "unknown");
        log.info("Processed Razorpay webhook event: {}", eventType);
    }
}
