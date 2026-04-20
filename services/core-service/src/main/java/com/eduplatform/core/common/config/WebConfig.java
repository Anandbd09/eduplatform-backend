package com.eduplatform.core.common.config;

import com.eduplatform.core.media.config.MediaStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RequestInterceptor requestInterceptor;

    @Autowired
    private MediaStorageProperties mediaStorageProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestInterceptor);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!mediaStorageProperties.getLocal().isEnabled()) {
            return;
        }

        String basePath = mediaStorageProperties.getLocal().getPublicBasePath();
        String normalizedBasePath = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
        Path uploadDirectory = Paths.get(mediaStorageProperties.getLocal().getBaseDirectory())
                .toAbsolutePath()
                .normalize();

        registry.addResourceHandler(normalizedBasePath + "/**")
                .addResourceLocations(uploadDirectory.toUri().toString());
    }
}
