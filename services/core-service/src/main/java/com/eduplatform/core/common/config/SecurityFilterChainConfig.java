package com.eduplatform.core.common.config;

import com.eduplatform.core.common.security.HeaderAuthenticationFilter;
import com.eduplatform.core.media.config.MediaStorageProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
public class SecurityFilterChainConfig {

    private final HeaderAuthenticationFilter headerAuthenticationFilter;
    private final MediaStorageProperties mediaStorageProperties;

    public SecurityFilterChainConfig(
            HeaderAuthenticationFilter headerAuthenticationFilter,
            MediaStorageProperties mediaStorageProperties
    ) {
        this.headerAuthenticationFilter = headerAuthenticationFilter;
        this.mediaStorageProperties = mediaStorageProperties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String publicMediaPattern = normalizeMediaPattern();

        http
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(headerAuthenticationFilter, AnonymousAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/api/v1/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/**",
                                publicMediaPattern
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/courses").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/courses/media/upload").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/courses/*/modules").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/courses/*/publish").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/instructor/**").hasRole("INSTRUCTOR")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private String normalizeMediaPattern() {
        String basePath = mediaStorageProperties.getLocal().getPublicBasePath();
        String normalizedBasePath = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
        return normalizedBasePath + "/**";
    }
}
