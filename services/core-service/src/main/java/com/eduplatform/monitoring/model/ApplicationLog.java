package com.eduplatform.monitoring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "application_logs")
public class ApplicationLog {

    @Id
    private String id;

    @Indexed
    private String level; // INFO, DEBUG, WARN, ERROR, CRITICAL

    @Indexed
    private String category; // AUTH, COURSE, PAYMENT, SYSTEM, etc

    private String message;

    private String details;

    @Indexed
    private String userId; // User who triggered log

    private String ipAddress;

    private String userAgent;

    private String endpoint; // API endpoint called

    private String httpMethod; // GET, POST, PUT, DELETE

    private Integer httpStatus;

    @Indexed
    private LocalDateTime timestamp;

    private Long responseTimeMs; // How long request took

    private String exceptionType;

    private String stackTrace;

    private String correlationId; // Track related logs

    private String sessionId;

    private String tenantId;

    private String environment; // DEV, STAGING, PROD

    private String version; // App version

    private String requestBody; // First 1000 chars

    private String responseBody; // First 1000 chars

    private Integer requestSize; // Bytes

    private Integer responseSize; // Bytes

    private Long version_field = 0L;

    /**
     * Is this log an error/critical
     */
    public boolean isErrorLevel() {
        return "ERROR".equals(level) || "CRITICAL".equals(level);
    }

    /**
     * Is response time slow (>1000ms)
     */
    public boolean isSlowResponse() {
        return responseTimeMs != null && responseTimeMs > 1000;
    }
}