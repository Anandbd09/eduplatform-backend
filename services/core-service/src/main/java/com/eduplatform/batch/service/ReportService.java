package com.eduplatform.batch.service;

import com.eduplatform.batch.model.BatchJob;
import com.eduplatform.batch.repository.BatchJobResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReportService {

    @Autowired
    private BatchJobResultRepository resultRepository;

    /**
     * GENERATE BATCH REPORT
     */
    public String generateReport(BatchJob job, String tenantId) {
        try {
            StringBuilder report = new StringBuilder();
            report.append("=== BATCH REPORT ===\n");
            report.append("Job ID: ").append(job.getJobId()).append("\n");
            report.append("Type: ").append(job.getJobType()).append("\n");
            report.append("Status: ").append(job.getStatus()).append("\n");
            report.append("Total Records: ").append(job.getTotalRecords()).append("\n");
            report.append("Processed: ").append(job.getProcessedRecords()).append("\n");
            report.append("Success: ").append(job.getSuccessRecords()).append("\n");
            report.append("Failed: ").append(job.getFailedRecords()).append("\n");
            report.append("Progress: ").append(String.format("%.2f%%", job.getProgressPercentage())).append("\n");

            log.info("Report generated for job: {}", job.getJobId());
            return report.toString();

        } catch (Exception e) {
            log.error("Error generating report", e);
            throw new RuntimeException("Failed to generate report");
        }
    }
}