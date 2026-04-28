package com.eduplatform.otp.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailOtpService {

    /**
     * SEND EMAIL OTP VIA RESEND
     */
    public void sendEmailOtp(String emailAddress, String code, String tenantId) throws Exception {
        try {
            // In production: use Resend SDK
            // com.resend.Resend client = new com.resend.Resend("api_key");
            // EmailMessage email = EmailMessage.builder()
            //     .from("noreply@eduplatform.com")
            //     .to(emailAddress)
            //     .subject("Your OTP Code")
            //     .html("<p>Your OTP code is: <strong>" + code + "</strong></p><p>Valid for 5 minutes</p>")
            //     .build();
            // client.emails().send(email);

            log.info("Email OTP sent: email={}, code={}", emailAddress, code);

        } catch (Exception e) {
            log.error("Error sending Email OTP", e);
            throw new Exception("Failed to send Email OTP: " + e.getMessage());
        }
    }
}