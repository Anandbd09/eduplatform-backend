// FILE 18: ExportDownloadResponse.java
package com.eduplatform.export.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ExportDownloadResponse {
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private String filePath;
    private String downloadUrl;
}