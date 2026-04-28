package com.eduplatform.monitoring.service;

import com.eduplatform.monitoring.util.LogFormatter;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoggingService {

    /**
     * FORMAT LOG MESSAGE
     */
    public String formatLogMessage(String level, String category, String message) {
        return LogFormatter.format(level, category, message);
    }

    /**
     * LOG WITH CONTEXT
     */
    public void logWithContext(String level, String message, String userId, String endpoint) {
        String formatted = String.format("[%s] [%s] [User:%s] [Endpoint:%s] %s",
                level, LocalDateTime.now(), userId, endpoint, message);

        if ("ERROR".equals(level) || "CRITICAL".equals(level)) {
            log.error(formatted);
        } else if ("WARN".equals(level)) {
            log.warn(formatted);
        } else if ("DEBUG".equals(level)) {
            log.debug(formatted);
        } else {
            log.info(formatted);
        }
    }
}
