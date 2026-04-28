package com.eduplatform.otp.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SmsOtpService {

    /**
     * SEND SMS OTP VIA TWILIO
     */
    public void sendSmsOtp(String phoneNumber, String code, String tenantId) throws Exception {
        try {
            // In production: use Twilio SDK
            // TwilioRestClient client = Twilio.getRestClient();
            // Message message = Message.creator(
            //     new PhoneNumber("+1xxxx..."),  // To number
            //     new PhoneNumber("+1yyyy..."),  // From number
            //     "Your OTP code is: " + code)
            //     .create();

            log.info("SMS OTP sent: phone={}, code={}", phoneNumber, code);

        } catch (Exception e) {
            log.error("Error sending SMS OTP", e);
            throw new Exception("Failed to send SMS OTP: " + e.getMessage());
        }
    }
}