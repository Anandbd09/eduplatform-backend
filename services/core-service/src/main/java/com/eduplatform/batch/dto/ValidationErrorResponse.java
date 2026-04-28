// FILE 27: ValidationErrorResponse.java
package com.eduplatform.batch.dto;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ValidationErrorResponse {
    private Integer recordNumber;
    private String recordIdentifier;
    private List<String> errors;
}