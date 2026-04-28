package com.eduplatform.export.service;

import com.eduplatform.export.model.ExportJob;
import com.eduplatform.export.util.ExcelBuilder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.*;

@Slf4j
@Service
public class ExcelExportService {

    /**
     * EXPORT TO EXCEL
     */
    public void exportToExcel(ExportJob job, String tenantId) throws Exception {
        try {
            ExcelBuilder excelBuilder = new ExcelBuilder();

            // Create sheet based on export type
            switch (job.getExportType()) {
                case "USER_DATA":
                    excelBuilder.createSheet("Users");
                    excelBuilder.addHeader("userId", "email", "firstName", "lastName", "role", "createdAt");
                    break;
                case "COURSE_DATA":
                    excelBuilder.createSheet("Courses");
                    excelBuilder.addHeader("courseId", "title", "description", "instructor", "price", "createdAt");
                    break;
                case "PAYMENT_DATA":
                    excelBuilder.createSheet("Payments");
                    excelBuilder.addHeader("orderId", "userId", "amount", "currency", "status", "createdAt");
                    break;
                default:
                    excelBuilder.createSheet("Data");
                    excelBuilder.addHeader("id", "data");
            }

            // In production: fetch actual data
            List<Map<String, String>> data = new ArrayList<>();
            // TODO: Fetch data based on filters

            // Add rows
            data.forEach(excelBuilder::addRow);

            // Generate Excel
            byte[] excelContent = excelBuilder.build();

            // Save to storage
            String filePath = "/exports/" + job.getJobId() + ".xlsx";

            job.setFilePath(filePath);
            job.setDownloadUrl("http://localhost:8081/api/v1/export/download/" + job.getJobId());
            job.setFileSize((long) excelContent.length);
            job.setExportedRecords(data.size());

            log.info("Excel export created: jobId={}, records={}", job.getJobId(), data.size());

        } catch (Exception e) {
            throw new Exception("Failed to export to Excel: " + e.getMessage());
        }
    }
}