// FILE 26: ExportValidator.java
package com.eduplatform.export.util;

import com.eduplatform.export.dto.ExportJobRequest;
import com.eduplatform.export.exception.ExportException;

public class ExportValidator {

    /**
     * VALIDATE EXPORT REQUEST
     */
    public static void validateExportRequest(ExportJobRequest request) throws ExportException {
        if (request == null) {
            throw new ExportException("Export request cannot be null");
        }

        if (request.getExportType() == null || request.getExportType().isEmpty()) {
            throw new ExportException("Export type is required");
        }

        if (request.getFormat() == null || request.getFormat().isEmpty()) {
            throw new ExportException("Export format is required");
        }

        // Validate export type
        String[] validTypes = {"USER_DATA", "COURSE_DATA", "PAYMENT_DATA", "GDPR_DATA"};
        boolean validType = false;
        for (String type : validTypes) {
            if (type.equals(request.getExportType())) {
                validType = true;
                break;
            }
        }
        if (!validType) {
            throw new ExportException("Invalid export type: " + request.getExportType());
        }

        // Validate format
        String[] validFormats = {"CSV", "PDF", "EXCEL", "JSON"};
        boolean validFormat = false;
        for (String format : validFormats) {
            if (format.equals(request.getFormat())) {
                validFormat = true;
                break;
            }
        }
        if (!validFormat) {
            throw new ExportException("Invalid export format: " + request.getFormat());
        }
    }
}