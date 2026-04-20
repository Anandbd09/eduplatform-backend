package com.eduplatform.payment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentConfig {

    @Value("${razorpay.key-id}")
    public String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    public String razorpayKeySecret;

    @Value("${razorpay.webhook-secret}")
    public String webhookSecret;
}