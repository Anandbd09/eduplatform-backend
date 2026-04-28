// FILE 22: TemplateResponse.java
package com.eduplatform.certificate.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TemplateResponse {
    private String id;
    private String name;
    private String description;
    private String status;
    private String language;
}