// FILE 21: TemplateRequest.java
package com.eduplatform.certificate.dto;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TemplateRequest {
    private String name;
    private String description;
    private String templateDesignUrl;
    private String borderDesign;
    private String backgroundColor;
    private String fontFamily;
    private String signature;
    private String seal;
    private List<String> customFieldNames;
    private String language;
}