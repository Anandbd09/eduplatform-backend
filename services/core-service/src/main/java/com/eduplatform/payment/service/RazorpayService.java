package com.eduplatform.payment.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@Slf4j
@Service
public class RazorpayService {

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    @Value("${app.webhook-url}")
    private String webhookUrl;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void init() {
        try {
            this.razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        } catch (RazorpayException e) {
            log.error("Error initializing Razorpay client", e);
        }
    }

    // Create Order
    public JSONObject createOrder(BigDecimal amount, String currency, String receipt, Map<String, String> notes)
            throws RazorpayException {
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount.multiply(new BigDecimal(100)).longValue()); // Convert to paise
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", receipt);
        orderRequest.put("notes", notes);

        return razorpayClient.orders.create(orderRequest).toJson();
    }

    // Capture Payment
    public JSONObject capturePayment(String paymentId, BigDecimal amount) throws RazorpayException {
        JSONObject captureRequest = new JSONObject();
        captureRequest.put("amount", amount.multiply(new BigDecimal(100)).longValue());

        return razorpayClient.payments.capture(paymentId, captureRequest).toJson();
    }

    // Verify Payment Signature
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            String expectedSignature = generateHmac(payload, razorpayKeySecret);
            return MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Error verifying signature", e);
            return false;
        }
    }

    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            String expectedSignature = generateHmac(payload, razorpayKeySecret);
            return MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    // Create Subscription
    public JSONObject createSubscription(String planId, Integer totalCount, Map<String, String> notes)
            throws RazorpayException {
        JSONObject subscriptionRequest = new JSONObject();
        subscriptionRequest.put("plan_id", planId);
        subscriptionRequest.put("customer_notify", 1);
        subscriptionRequest.put("total_count", totalCount);
        subscriptionRequest.put("notes", notes);
        subscriptionRequest.put("notify_info", new JSONObject()
                .put("notify_phone", 1)
                .put("notify_email", 1));

        return razorpayClient.subscriptions.create(subscriptionRequest).toJson();
    }

    // Pause Subscription
    public JSONObject pauseSubscription(String subscriptionId) throws RazorpayException {
        JSONObject request = new JSONObject();
        request.put("pause_at", "now");

        return razorpayClient.subscriptions.pause(subscriptionId, request).toJson();
    }

    // Cancel Subscription
    public JSONObject cancelSubscription(String subscriptionId, String notes) throws RazorpayException {
        JSONObject request = new JSONObject();
        request.put("notes", notes);

        return razorpayClient.subscriptions.cancel(subscriptionId, request).toJson();
    }

    // Create Refund
    public JSONObject createRefund(String paymentId, BigDecimal amount, String notes) throws RazorpayException {
        JSONObject refundRequest = new JSONObject();
        if (amount != null) {
            refundRequest.put("amount", amount.multiply(new BigDecimal(100)).longValue());
        }
        refundRequest.put("notes", notes);
        refundRequest.put("receipt", "REFUND-" + System.currentTimeMillis());

        return razorpayClient.payments.refund(paymentId, refundRequest).toJson();
    }

    // Get Payment Details
    public JSONObject getPaymentDetails(String paymentId) throws RazorpayException {
        return razorpayClient.payments.fetch(paymentId).toJson();
    }

    // Get Order Details
    public JSONObject getOrderDetails(String orderId) throws RazorpayException {
        return razorpayClient.orders.fetch(orderId).toJson();
    }

    private String generateHmac(String payload, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
