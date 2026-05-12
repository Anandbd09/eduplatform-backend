package com.eduplatform.notification.service;

import com.eduplatform.notification.config.RabbitMQConfig;
import com.eduplatform.notification.dto.NotificationEventPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationEventListener {

    @Autowired
    private EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_COURSE_SUBMITTED)
    public void handleCourseSubmitted(NotificationEventPayload payload) {
        try {
            log.info("Handling COURSE_SUBMITTED for courseId={}", payload.getCourseId());
            emailService.sendCourseSubmittedEmail(
                    payload.getTargetEmail(),
                    payload.getCourseTitle(),
                    payload.getInstructorName()
            );
        } catch (Exception e) {
            log.error("Failed to handle COURSE_SUBMITTED for courseId={}", payload.getCourseId(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_COURSE_APPROVED)
    public void handleCourseApproved(NotificationEventPayload payload) {
        try {
            log.info("Handling COURSE_APPROVED for courseId={}", payload.getCourseId());
            emailService.sendCourseApprovedEmail(
                    payload.getInstructorEmail(),
                    payload.getInstructorName(),
                    payload.getCourseTitle(),
                    payload.getNotes()
            );
        } catch (Exception e) {
            log.error("Failed to handle COURSE_APPROVED for courseId={}", payload.getCourseId(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_COURSE_REJECTED)
    public void handleCourseRejected(NotificationEventPayload payload) {
        try {
            log.info("Handling COURSE_REJECTED for courseId={}", payload.getCourseId());
            emailService.sendCourseRejectedEmail(
                    payload.getInstructorEmail(),
                    payload.getInstructorName(),
                    payload.getCourseTitle(),
                    payload.getNotes()
            );
        } catch (Exception e) {
            log.error("Failed to handle COURSE_REJECTED for courseId={}", payload.getCourseId(), e);
        }
    }
}
