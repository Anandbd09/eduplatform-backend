package com.eduplatform.core.media.service;

import com.eduplatform.core.media.model.StoredMedia;

public interface StorageProvider {

    String getProviderId();

    StoredMedia store(MediaStorageRequest request);
}
