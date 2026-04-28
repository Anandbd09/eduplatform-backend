// FILE 25: MetricsCalculator.java
package com.eduplatform.monitoring.util;

import java.util.List;
import java.util.Collections;

public class MetricsCalculator {

    /**
     * CALCULATE PERCENTILE
     */
    public static Long calculatePercentile(List<Long> values, double percentile) {
        if (values.isEmpty()) return 0L;
        Collections.sort(values);
        int index = (int) Math.ceil(percentile * values.size()) - 1;
        return values.get(Math.max(0, Math.min(index, values.size() - 1)));
    }

    /**
     * CALCULATE AVERAGE
     */
    public static Double calculateAverage(List<Long> values) {
        if (values.isEmpty()) return 0.0;
        return values.stream().mapToLong(Long::longValue).average().orElse(0.0);
    }
}