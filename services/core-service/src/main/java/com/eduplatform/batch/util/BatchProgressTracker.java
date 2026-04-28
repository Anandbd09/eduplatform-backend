// FILE 33: BatchProgressTracker.java
package com.eduplatform.batch.util;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BatchProgressTracker {
    private String jobId;
    private Integer totalRecords;
    private Integer processedRecords;
    private Integer successRecords;
    private Integer failedRecords;

    /**
     * GET PROGRESS PERCENTAGE
     */
    public Double getProgressPercentage() {
        if (totalRecords == null || totalRecords == 0) {
            return 0.0;
        }
        return ((double) (processedRecords != null ? processedRecords : 0) / totalRecords) * 100;
    }

    /**
     * GET SUCCESS RATE
     */
    public Double getSuccessRate() {
        if (processedRecords == null || processedRecords == 0) {
            return 0.0;
        }
        return ((double) (successRecords != null ? successRecords : 0) / processedRecords) * 100;
    }
}