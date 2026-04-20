package com.eduplatform.core.media.service;

import com.eduplatform.core.common.exception.AppException;
import com.eduplatform.core.media.config.MediaStorageProperties;
import com.eduplatform.core.media.model.MediaAsset;
import com.eduplatform.core.media.model.MediaUploadCategory;
import com.eduplatform.core.media.model.StoredMedia;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class MediaStorageService {

    private final MediaStorageProperties mediaStorageProperties;
    private final Map<String, StorageProvider> providersById;

    public MediaStorageService(
            MediaStorageProperties mediaStorageProperties,
            List<StorageProvider> storageProviders
    ) {
        this.mediaStorageProperties = mediaStorageProperties;
        this.providersById = new LinkedHashMap<>();
        for (StorageProvider storageProvider : storageProviders) {
            providersById.put(storageProvider.getProviderId(), storageProvider);
        }
    }

    public MediaAsset upload(
            MultipartFile file,
            MediaUploadCategory category,
            String tenantId,
            String actorId,
            String requestedProvider
    ) {
        if (file == null || file.isEmpty()) {
            throw AppException.badRequest("File is required");
        }

        String primaryProviderId = StringUtils.hasText(requestedProvider)
                ? requestedProvider.trim()
                : mediaStorageProperties.getPrimaryProvider();

        MediaStorageRequest request = MediaStorageRequest.builder()
                .file(file)
                .category(category)
                .tenantId(tenantId)
                .actorId(actorId)
                .build();

        StoredMedia primary = getRequiredProvider(primaryProviderId).store(request);
        List<StoredMedia> replicas = storeReplicas(primaryProviderId, request);

        return MediaAsset.builder()
                .primary(primary)
                .replicas(replicas)
                .build();
    }

    private List<StoredMedia> storeReplicas(String primaryProviderId, MediaStorageRequest request) {
        List<StoredMedia> replicas = new ArrayList<>();
        for (String replicaProviderId : mediaStorageProperties.getReplicaProviders()) {
            if (!StringUtils.hasText(replicaProviderId) || Objects.equals(replicaProviderId, primaryProviderId)) {
                continue;
            }

            StorageProvider storageProvider = providersById.get(replicaProviderId);
            if (storageProvider == null) {
                log.warn("Replica provider '{}' is configured but no implementation is registered", replicaProviderId);
                continue;
            }

            try {
                replicas.add(storageProvider.store(request));
            } catch (RuntimeException ex) {
                log.warn("Replica upload failed for provider '{}': {}", replicaProviderId, ex.getMessage());
            }
        }

        return replicas;
    }

    private StorageProvider getRequiredProvider(String providerId) {
        StorageProvider storageProvider = providersById.get(providerId);
        if (storageProvider == null) {
            throw AppException.badRequest("Unsupported media storage provider: " + providerId);
        }
        return storageProvider;
    }
}
