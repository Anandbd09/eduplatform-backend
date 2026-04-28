package com.eduplatform.export.service;

import com.eduplatform.export.model.ExportJob;
import com.eduplatform.export.util.PdfGenerator;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PdfExportService {

    /**
     * EXPORT TO PDF
     */
    public void exportToPdf(ExportJob job, String tenantId) throws Exception {
        try {
            PdfGenerator pdfGenerator = new PdfGenerator();

            // Build PDF based on export type
            switch (job.getExportType()) {
                case "USER_DATA":
                    pdfGenerator.addTitle("User Export Report");
                    pdfGenerator.addText("Export Date: " + java.time.LocalDateTime.now());
                    break;
                case "PAYMENT_DATA":
                    pdfGenerator.addTitle("Invoice Report");
                    pdfGenerator.addText("Report Generated: " + java.time.LocalDateTime.now());
                    break;
                default:
                    pdfGenerator.addTitle("Data Export");
            }

            // In production: add actual data tables
            pdfGenerator.addText("Total Records: " + (job.getTotalRecords() != null ? job.getTotalRecords() : 0));

            // Generate PDF
            byte[] pdfContent = pdfGenerator.generate();

            // Save to storage
            String filePath = "/exports/" + job.getJobId() + ".pdf";

            job.setFilePath(filePath);
            job.setDownloadUrl("http://localhost:8081/api/v1/export/download/" + job.getJobId());
            job.setFileSize((long) pdfContent.length);

            log.info("PDF export created: jobId={}, size={} bytes", job.getJobId(), pdfContent.length);

        } catch (Exception e) {
            throw new Exception("Failed to export to PDF: " + e.getMessage());
        }
    }
}