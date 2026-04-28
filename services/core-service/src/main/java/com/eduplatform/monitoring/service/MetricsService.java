package com.eduplatform.monitoring.service;

import com.eduplatform.monitoring.util.MetricsCalculator;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MetricsService {

    /**
     * CALCULATE RESPONSE TIME PERCENTILE
     */
    public Long calculateP95(java.util.List<Long> responseTimes) {
        if (responseTimes.isEmpty()) return 0L;
        java.util.Collections.sort(responseTimes);
        int index = (int) Math.ceil(0.95 * responseTimes.size()) - 1;
        return responseTimes.get(index);
    }

    /**
     * CALCULATE ERROR RATE
     */
    public Double calculateErrorRate(Long totalRequests, Long errorCount) {
        if (totalRequests == 0) return 0.0;
        return (errorCount.doubleValue() / totalRequests) * 100;
    }

    /**
     * CALCULATE UPTIME
     */
    public Double calculateUptime(Long totalTime, Long downtime) {
        if (totalTime == 0) return 100.0;
        return ((totalTime - downtime) / (double) totalTime) * 100;
    }
}
