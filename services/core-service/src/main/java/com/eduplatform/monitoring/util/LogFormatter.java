// FILE 24: LogFormatter.java
package com.eduplatform.monitoring.util;

import java.time.LocalDateTime;

public class LogFormatter {

    /**
     * FORMAT LOG MESSAGE
     */
    public static String format(String level, String category, String message) {
        return String.format("[%s] [%s] [%s] %s",
                LocalDateTime.now(),
                level.toUpperCase(),
                category.toUpperCase(),
                message
        );
    }

    /**
     * FORMAT WITH CONTEXT
     */
    public static String formatWithContext(String level, String message,
                                           String userId, String endpoint) {
        return String.format("[%s] [%s] [User:%s] [Endpoint:%s] %s",
                LocalDateTime.now(),
                level.toUpperCase(),
                userId,
                endpoint,
                message
        );
    }
}