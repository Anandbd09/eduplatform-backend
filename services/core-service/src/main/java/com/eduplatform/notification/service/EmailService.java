package com.eduplatform.notification.service;

import com.eduplatform.notification.model.EmailTemplate;
import com.eduplatform.notification.repository.EmailTemplateRepository;
import com.eduplatform.core.user.model.User;
import com.eduplatform.payment.model.Order;
import com.eduplatform.payment.model.Payment;
import com.eduplatform.payment.model.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;
import jakarta.mail.internet.MimeMessage;

import org.springframework.http.HttpHeaders;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class EmailService {

    @Value("${email.provider}") // resend, sendgrid, etc
    private String emailProvider;

    @Value("${resend.api-key:}")
    private String resendApiKey;

    @Value("${resend.from-email:onboarding@resend.dev}")
    private String resendFromEmail;

    @Value("${resend.from-name:EduPlatform}")
    private String resendFromName;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Value("${smtp.from-email:${spring.mail.username:}}")
    private String smtpFromEmail;

    @Value("${smtp.from-name:EduPlatform}")
    private String smtpFromName;

    @Autowired
    private EmailTemplateRepository templateRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JavaMailSender javaMailSender;

    // Send Email using Resend
    public String sendEmailViaResend(String to, String subject, String htmlContent,
                                     String fromEmail, String fromName) {
        try {
            if (!StringUtils.hasText(resendApiKey)) {
                throw new IllegalStateException("Resend API key is not configured");
            }

            String effectiveFromEmail = StringUtils.hasText(fromEmail) ? fromEmail : resendFromEmail;
            String effectiveFromName = StringUtils.hasText(fromName) ? fromName : resendFromName;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + resendApiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("from", effectiveFromName + " <" + effectiveFromEmail + ">");
            body.put("to", List.of(to));
            body.put("subject", subject);
            body.put("html", htmlContent);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.resend.com/emails",
                    request,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            String messageId = (String) responseBody.get("id");

            log.info("Email sent successfully: {}", messageId);
            return messageId;

        } catch (Exception e) {
            log.error("Error sending email via Resend", e);
            throw new RuntimeException("Failed to send email");
        }
    }

    public String sendEmailViaSmtp(String to, String subject, String htmlContent,
                                   String fromEmail, String fromName) {
        try {
            String effectiveFromEmail = StringUtils.hasText(fromEmail) ? fromEmail : smtpFromEmail;
            String effectiveFromName = StringUtils.hasText(fromName) ? fromName : smtpFromName;

            if (!StringUtils.hasText(smtpUsername)) {
                throw new IllegalStateException("SMTP username is not configured");
            }
            if (!StringUtils.hasText(effectiveFromEmail)) {
                throw new IllegalStateException("SMTP from email is not configured");
            }

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(effectiveFromEmail, effectiveFromName);

            javaMailSender.send(message);
            log.info("Email sent successfully via SMTP to: {}", to);
            return "smtp:" + System.currentTimeMillis();
        } catch (Exception e) {
            log.error("Error sending email via SMTP", e);
            throw new RuntimeException("Failed to send email");
        }
    }

    public String sendEmail(String to, String subject, String htmlContent,
                            String fromEmail, String fromName) {
        if ("smtp".equalsIgnoreCase(emailProvider)) {
            return sendEmailViaSmtp(to, subject, htmlContent, fromEmail, fromName);
        }
        if ("resend".equalsIgnoreCase(emailProvider)) {
            return sendEmailViaResend(to, subject, htmlContent, fromEmail, fromName);
        }
        throw new IllegalStateException("Unsupported email provider: " + emailProvider);
    }

    public void sendPasswordResetEmail(String email, String firstName, String resetUrl, long expiryMinutes) {
        String recipientName = StringUtils.hasText(firstName) ? firstName : "there";
        String subject = "Reset your EduPlatform password";
        String htmlContent = """
                <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #111827;">
                  <p>Hi %s,</p>
                  <p>We received a request to reset your EduPlatform password.</p>
                  <p>
                    <a href="%s" style="display: inline-block; padding: 12px 20px; background: #111827; color: #ffffff; text-decoration: none; border-radius: 6px;">
                      Reset Password
                    </a>
                  </p>
                  <p>This link will expire in %d minutes.</p>
                  <p>If you did not request this, you can ignore this email.</p>
                </div>
                """.formatted(recipientName, resetUrl, expiryMinutes);

        sendEmail(email, subject, htmlContent, null, null);
    }

    // Send Email with Template
    public String sendEmailWithTemplate(String to, String templateName,
                                        Map<String, String> variables, String fromEmail, String fromName) {
        try {
            EmailTemplate template = templateRepository.findByTemplateName(templateName)
                    .orElseThrow(() -> new RuntimeException("Template not found: " + templateName));

            // Replace variables
            String htmlContent = template.getHtmlContent();
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                htmlContent = htmlContent.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }

            return sendEmail(to, template.getSubject(), htmlContent,
                    template.getFromEmail(), template.getFromName());

        } catch (Exception e) {
            log.error("Error sending templated email", e);
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    // Send Welcome Email
    public void sendWelcomeEmail(String email, String firstName) {
        Map<String, String> variables = new HashMap<>();
        variables.put("firstName", firstName);

        sendEmailWithTemplate(email, "WELCOME_EMAIL", variables,
                "no-reply@eduplatform.com", "EduPlatform");
    }

    // Send Course Enrollment Email
    public void sendCourseEnrollmentEmail(String email, String courseName,
                                          String instructorName, String accessUrl) {
        Map<String, String> variables = new HashMap<>();
        variables.put("courseName", courseName);
        variables.put("instructorName", instructorName);
        variables.put("accessUrl", accessUrl);

        sendEmailWithTemplate(email, "COURSE_ENROLLMENT", variables,
                "no-reply@eduplatform.com", "EduPlatform");
    }

    // Send Payment Receipt Email
    public void sendPaymentReceiptEmail(String email, String invoiceNumber,
                                        String amount, String courseName, String downloadUrl) {
        Map<String, String> variables = new HashMap<>();
        variables.put("invoiceNumber", invoiceNumber);
        variables.put("amount", amount);
        variables.put("courseName", courseName);
        variables.put("downloadUrl", downloadUrl);

        sendEmailWithTemplate(email, "PAYMENT_RECEIPT", variables,
                "payments@eduplatform.com", "EduPlatform Payments");
    }

    // Send Certificate Email
    public void sendCertificateEmail(String email, String studentName,
                                     String courseName, String certificateUrl) {
        Map<String, String> variables = new HashMap<>();
        variables.put("studentName", studentName);
        variables.put("courseName", courseName);
        variables.put("certificateUrl", certificateUrl);

        sendEmailWithTemplate(email, "CERTIFICATE_EARNED", variables,
                "no-reply@eduplatform.com", "EduPlatform");
    }

    // Send Live Session Reminder
    public void sendLiveSessionReminder(String email, String sessionTitle,
                                        String instructorName, String startTime, String joinUrl) {
        Map<String, String> variables = new HashMap<>();
        variables.put("sessionTitle", sessionTitle);
        variables.put("instructorName", instructorName);
        variables.put("startTime", startTime);
        variables.put("joinUrl", joinUrl);

        sendEmailWithTemplate(email, "LIVE_SESSION_REMINDER", variables,
                "no-reply@eduplatform.com", "EduPlatform");
    }

    // Send Weekly Digest
    public void sendWeeklyDigest(String email, String firstName,
                                 String hoursLearned, String lessonsCompleted, String upcomingSessions) {
        Map<String, String> variables = new HashMap<>();
        variables.put("firstName", firstName);
        variables.put("hoursLearned", hoursLearned);
        variables.put("lessonsCompleted", lessonsCompleted);
        variables.put("upcomingSessions", upcomingSessions);

        sendEmailWithTemplate(email, "WEEKLY_DIGEST", variables,
                "no-reply@eduplatform.com", "EduPlatform");
    }

    public void sendPaymentSuccessEmail(User user, Order order, Payment payment) {
        if (user == null || user.getEmail() == null) {
            return;
        }

        sendPaymentReceiptEmail(
                user.getEmail(),
                payment.getReceiptId(),
                payment.getAmount().toPlainString(),
                order.getCourseId(),
                "/api/v1/payments/invoices/" + payment.getId() + "/download"
        );
    }

    public void sendRefundEmail(User user, Payment payment, java.math.BigDecimal refundAmount) {
        log.info("Refund email queued for user={}, payment={}, amount={}",
                user != null ? user.getId() : null,
                payment != null ? payment.getId() : null,
                refundAmount);
    }

    public void sendSubscriptionCancelledEmail(User user, Subscription subscription) {
        log.info("Subscription cancellation email queued for user={}, subscription={}",
                user != null ? user.getId() : null,
                subscription != null ? subscription.getId() : null);
    }

    public void sendSubscriptionRenewalReminder(User user, Subscription subscription) {
        log.info("Subscription renewal reminder queued for user={}, subscription={}",
                user != null ? user.getId() : null,
                subscription != null ? subscription.getId() : null);
    }
}
