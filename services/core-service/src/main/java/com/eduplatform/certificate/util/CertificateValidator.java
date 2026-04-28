// FILE 25: CertificateValidator.java
package com.eduplatform.certificate.util;
import com.eduplatform.certificate.dto.CertificateRequest;

public class CertificateValidator {

    public static void validateCertificateRequest(CertificateRequest request) {
        if (request.getUserId() == null || request.getUserId().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (request.getCourseId() == null || request.getCourseId().isEmpty()) {
            throw new IllegalArgumentException("Course ID is required");
        }
        if (request.getUserName() == null || request.getUserName().isEmpty()) {
            throw new IllegalArgumentException("User name is required");
        }
        if (request.getCourseCompletion() == null || request.getCourseCompletion() < 0) {
            throw new IllegalArgumentException("Valid course completion required");
        }
    }
}