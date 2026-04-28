// FILE 34: ReportGenerator.java
package com.eduplatform.batch.util;

public class ReportGenerator {

    /**
     * GENERATE SUMMARY REPORT
     */
    public static String generateSummary(String jobId, Integer total, Integer success,
                                         Integer failed, Double progressPercentage) {
        StringBuilder report = new StringBuilder();
        report.append("=== BATCH JOB SUMMARY ===\n");
        report.append("Job ID: ").append(jobId).append("\n");
        report.append("Total Records: ").append(total).append("\n");
        report.append("Success: ").append(success).append("\n");
        report.append("Failed: ").append(failed).append("\n");
        report.append("Progress: ").append(String.format("%.2f%%", progressPercentage)).append("\n");
        report.append("Success Rate: ").append(String.format("%.2f%%",
                total > 0 ? ((double) success / total) * 100 : 0)).append("\n");

        return report.toString();
    }
}