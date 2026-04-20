package com.eduplatform.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    private final ResourceLoader resourceLoader;

    @Value("${firebase.config}")
    private String firebaseConfigPath;

    public FirebaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void initialize() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        if (firebaseConfigPath == null || firebaseConfigPath.isBlank()) {
            log.warn("Firebase config path is not set. Push notifications are disabled.");
            return;
        }

        try (InputStream serviceAccount = openFirebaseConfig()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase initialized using config: {}", firebaseConfigPath);
        } catch (Exception e) {
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }

    private InputStream openFirebaseConfig() throws IOException {
        Resource resource = resourceLoader.getResource(firebaseConfigPath);
        if (!resource.exists()) {
            throw new IOException("Firebase config not found at: " + firebaseConfigPath);
        }

        return resource.getInputStream();
    }
}
