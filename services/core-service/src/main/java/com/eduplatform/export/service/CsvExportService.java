package com.eduplatform.export.service;

import com.eduplatform.export.model.ExportJob;
import com.eduplatform.export.util.CsvBuilder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.*;

@Slf4j
@Service
public class CsvExportService {

    /**
     * EXPORT TO CSV
     */
    public void exportToCsv(ExportJob job, String tenantId) throws Exception {
        try {
            CsvBuilder csvBuilder = new CsvBuilder();

            // Add headers based on export type
            switch (job.getExportType()) {
                case "USER_DATA":
                    csvBuilder.addHeader("userId", "email", "firstName", "lastName", "role", "createdAt");
                    break;
                case "COURSE_DATA":
                    csvBuilder.addHeader("courseId", "title", "description", "instructor", "price", "createdAt");
                    break;
                case "PAYMENT_DATA":
                    csvBuilder.addHeader("orderId", "userId", "amount", "currency", "status", "createdAt");
                    break;
                default:
                    csvBuilder.addHeader("id", "data");
            }

            // In production: fetch actual data from database/services
            List<Map<String, String>> data = new ArrayList<>();
            // TODO: Fetch data based on filters

            // Add rows
            data.forEach(csvBuilder::addRow);

            // Generate file
            String csvContent = csvBuilder.build();

            // Save to storage
            String filePath = "/exports/" + job.getJobId() + ".csv";

            job.setFilePath(filePath);
            job.setDownloadUrl("http://localhost:8081/api/v1/export/download/" + job.getJobId());
            job.setFileSize((long) csvContent.getBytes().length);
            job.setExportedRecords(data.size());

            log.info("CSV export created: jobId={}, records={}", job.getJobId(), data.size());

        } catch (Exception e) {
            throw new Exception("Failed to export to CSV: " + e.getMessage());
        }
    }
}