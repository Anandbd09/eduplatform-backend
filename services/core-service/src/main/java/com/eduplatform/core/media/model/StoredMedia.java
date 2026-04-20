package com.eduplatform.core.media.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoredMedia {
    private String provider;

    private String storageKey;

    private String publicUrl;

    private String originalFilename;

    private String contentType;

    private Long sizeBytes;

    private LocalDateTime uploadedAt;
}
