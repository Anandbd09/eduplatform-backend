package com.eduplatform.core.media.service;

import com.eduplatform.core.media.model.MediaUploadCategory;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
public class MediaStorageRequest {

    private final MultipartFile file;

    private final MediaUploadCategory category;

    private final String tenantId;

    private final String actorId;
}
