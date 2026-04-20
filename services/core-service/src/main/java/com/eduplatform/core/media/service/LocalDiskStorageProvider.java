package com.eduplatform.core.media.service;

import com.eduplatform.core.common.exception.AppException;
import com.eduplatform.core.media.config.MediaStorageProperties;
import com.eduplatform.core.media.model.StoredMedia;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalDiskStorageProvider implements StorageProvider {

    private final MediaStorageProperties mediaStorageProperties;

    @Override
    public String getProviderId() {
        return mediaStorageProperties.getLocal().getProviderId();
    }

    @Override
    public StoredMedia store(MediaStorageRequest request) {
        if (!mediaStorageProperties.getLocal().isEnabled()) {
            throw AppException.badRequest("Local media storage is disabled");
        }

        String tenantSegment = StringUtils.hasText(request.getTenantId()) ? request.getTenantId() : "default";
        String actorSegment = StringUtils.hasText(request.getActorId()) ? request.getActorId() : "system";
        String originalFilename = StringUtils.cleanPath(resolveOriginalFilename(request));
        String storedFilename = UUID.randomUUID() + resolveExtension(originalFilename);

        Path rootDirectory = resolveRootDirectory();
        Path relativePath = Paths.get(
                tenantSegment,
                request.getCategory().name().toLowerCase(Locale.ROOT),
                actorSegment,
                storedFilename
        );
        Path targetPath = rootDirectory.resolve(relativePath).normalize();

        if (!targetPath.startsWith(rootDirectory)) {
            throw AppException.badRequest("Invalid upload path");
        }

        try {
            Files.createDirectories(targetPath.getParent());
            try (InputStream inputStream = request.getFile().getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            log.error("Failed to store media on local disk", ex);
            throw AppException.internalError("Failed to store media file");
        }

        String normalizedKey = relativePath.toString().replace('\\', '/');

        return StoredMedia.builder()
                .provider(getProviderId())
                .storageKey(normalizedKey)
                .publicUrl(buildPublicUrl(normalizedKey))
                .originalFilename(originalFilename)
                .contentType(request.getFile().getContentType())
                .sizeBytes(request.getFile().getSize())
                .uploadedAt(LocalDateTime.now())
                .build();
    }

    private Path resolveRootDirectory() {
        return Paths.get(mediaStorageProperties.getLocal().getBaseDirectory())
                .toAbsolutePath()
                .normalize();
    }

    private String resolveOriginalFilename(MediaStorageRequest request) {
        String originalFilename = request.getFile().getOriginalFilename();
        return StringUtils.hasText(originalFilename) ? originalFilename : "upload.bin";
    }

    private String resolveExtension(String filename) {
        int extensionIndex = filename.lastIndexOf('.');
        return extensionIndex >= 0 ? filename.substring(extensionIndex) : "";
    }

    private String buildPublicUrl(String storageKey) {
        String basePath = mediaStorageProperties.getLocal().getPublicBasePath();
        String normalizedBasePath = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
        String relativeUrl = normalizedBasePath + "/" + storageKey;
        String publicBaseUrl = mediaStorageProperties.getPublicBaseUrl();

        if (!StringUtils.hasText(publicBaseUrl)) {
            return relativeUrl;
        }

        String normalizedPublicBaseUrl = publicBaseUrl.endsWith("/")
                ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                : publicBaseUrl;

        return normalizedPublicBaseUrl + relativeUrl;
    }
}
