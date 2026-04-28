package com.eduplatform.security.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class TwoFactorAuthService {

    /**
     * SEND OTP VIA SMS
     */
    public void sendOtpViaSms(String phoneNumber, String otp) {
        try {
            // In production: Use Twilio to send SMS
            // twilioClient.sendSms(phoneNumber, "Your OTP is: " + otp);
            log.info("OTP sent via SMS to: {}", phoneNumber);
        } catch (Exception e) {
            log.error("Error sending OTP via SMS", e);
            throw new RuntimeException("Failed to send OTP");
        }
    }

    /**
     * SEND OTP VIA EMAIL
     */
    public void sendOtpViaEmail(String email, String otp) {
        try {
            // In production: Use Resend to send email
            // resendClient.sendEmail(email, "Your OTP is: " + otp);
            log.info("OTP sent via email to: {}", email);
        } catch (Exception e) {
            log.error("Error sending OTP via email", e);
            throw new RuntimeException("Failed to send OTP");
        }
    }
}