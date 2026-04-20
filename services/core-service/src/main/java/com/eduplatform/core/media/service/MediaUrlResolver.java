package com.eduplatform.core.media.service;

import com.eduplatform.core.media.config.MediaStorageProperties;
import com.eduplatform.core.media.model.MediaAsset;
import com.eduplatform.core.media.model.StoredMedia;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MediaUrlResolver {

    private final MediaStorageProperties mediaStorageProperties;

    public String resolveUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return url;
        }

        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }

        String baseUrl = mediaStorageProperties.getPublicBaseUrl();
        if (!StringUtils.hasText(baseUrl)) {
            return url;
        }

        String normalizedBaseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;

        String normalizedPath = url.startsWith("/") ? url : "/" + url;
        return normalizedBaseUrl + normalizedPath;
    }

    public MediaAsset resolve(MediaAsset mediaAsset) {
        if (mediaAsset == null) {
            return null;
        }

        List<StoredMedia> resolvedReplicas = new ArrayList<>();
        if (mediaAsset.getReplicas() != null) {
            for (StoredMedia replica : mediaAsset.getReplicas()) {
                resolvedReplicas.add(resolve(replica));
            }
        }

        return MediaAsset.builder()
                .primary(resolve(mediaAsset.getPrimary()))
                .replicas(resolvedReplicas)
                .build();
    }

    public StoredMedia resolve(StoredMedia storedMedia) {
        if (storedMedia == null) {
            return null;
        }

        return StoredMedia.builder()
                .provider(storedMedia.getProvider())
                .storageKey(storedMedia.getStorageKey())
                .publicUrl(resolveUrl(storedMedia.getPublicUrl()))
                .originalFilename(storedMedia.getOriginalFilename())
                .contentType(storedMedia.getContentType())
                .sizeBytes(storedMedia.getSizeBytes())
                .uploadedAt(storedMedia.getUploadedAt())
                .build();
    }
}
