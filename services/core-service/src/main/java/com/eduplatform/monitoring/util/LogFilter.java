// FILE 27: LogFilter.java
package com.eduplatform.monitoring.util;

import com.eduplatform.monitoring.model.ApplicationLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class LogFilter {

    /**
     * FILTER LOGS BY LEVEL
     */
    public static List<ApplicationLog> filterByLevel(List<ApplicationLog> logs, String level) {
        return logs.stream()
                .filter(log -> level.equals(log.getLevel()))
                .collect(Collectors.toList());
    }

    /**
     * FILTER LOGS BY TIME RANGE
     */
    public static List<ApplicationLog> filterByTimeRange(List<ApplicationLog> logs,
                                                         LocalDateTime start,
                                                         LocalDateTime end) {
        return logs.stream()
                .filter(log -> !log.getTimestamp().isBefore(start) &&
                        !log.getTimestamp().isAfter(end))
                .collect(Collectors.toList());
    }
}