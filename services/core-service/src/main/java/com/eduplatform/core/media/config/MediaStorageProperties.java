package com.eduplatform.core.media.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app.media.storage")
public class MediaStorageProperties {

    private String primaryProvider = "local";

    private List<String> replicaProviders = new ArrayList<>();

    private String publicBaseUrl = "http://localhost:8080";

    private final LocalDisk local = new LocalDisk();

    @Data
    public static class LocalDisk {
        private boolean enabled = true;

        private String providerId = "local";

        private String baseDirectory = "uploads";

        private String publicBasePath = "/media";
    }
}
