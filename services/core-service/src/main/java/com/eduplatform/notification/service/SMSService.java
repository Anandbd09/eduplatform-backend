package com.eduplatform.notification.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SMSService {

    @Value("${twilio.account-sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token}")
    private String twilioAuthToken;

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    @PostConstruct
    public void init() {
        Twilio.init(twilioAccountSid, twilioAuthToken);
    }

    public String sendSMS(String phoneNumber, String messageContent) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    messageContent
            ).create();

            log.info("SMS sent successfully: {}", message.getSid());
            return message.getSid();
        } catch (Exception e) {
            log.error("Error sending SMS", e);
            throw new RuntimeException("Failed to send SMS");
        }
    }

    public String sendOTP(String phoneNumber, String otp) {
        String message = "Your EduPlatform OTP is: " + otp + ". Valid for 10 minutes.";
        return sendSMS(phoneNumber, message);
    }

    public String sendLiveSessionReminderSMS(String phoneNumber, String sessionTitle, String startTime) {
        String message = "Reminder: " + sessionTitle + " starts at " + startTime
                + ". Join now on EduPlatform!";
        return sendSMS(phoneNumber, message);
    }

    public String sendPaymentConfirmationSMS(String phoneNumber, String amount, String courseName) {
        String message = "Payment of Rs. " + amount + " for " + courseName
                + " confirmed. Welcome aboard!";
        return sendSMS(phoneNumber, message);
    }
}
