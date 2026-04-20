package com.eduplatform.notification.service;

import com.eduplatform.notification.dto.PreferenceResponse;
import com.eduplatform.notification.model.NotificationFrequency;
import com.eduplatform.notification.model.NotificationPreference;
import com.eduplatform.notification.repository.NotificationPreferenceRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PreferenceService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;

    @Transactional(readOnly = true)
    public PreferenceResponse getPreferences(String userId) {
        NotificationPreference preference = getOrCreatePreference(userId);
        return toResponse(preference);
    }

    @Transactional
    public PreferenceResponse updatePreferences(String userId, PreferenceResponse request) {
        NotificationPreference preference = getOrCreatePreference(userId);

        preference.setEmailEnabled(defaultIfNull(request.getEmailEnabled(), preference.getEmailEnabled()));
        preference.setSmsEnabled(defaultIfNull(request.getSmsEnabled(), preference.getSmsEnabled()));
        preference.setPushEnabled(defaultIfNull(request.getPushEnabled(), preference.getPushEnabled()));
        preference.setDndEnabled(defaultIfNull(request.getDndEnabled(), preference.getDndEnabled()));

        if (request.getDndStartTime() != null && !request.getDndStartTime().isBlank()) {
            preference.setDndStartTime(LocalTime.parse(request.getDndStartTime()));
        }

        if (request.getDndEndTime() != null && !request.getDndEndTime().isBlank()) {
            preference.setDndEndTime(LocalTime.parse(request.getDndEndTime()));
        }

        if (request.getDigestFrequency() != null && !request.getDigestFrequency().isBlank()) {
            preference.setDigestFrequency(NotificationFrequency.valueOf(request.getDigestFrequency().trim().toUpperCase()));
        }

        preference.setUpdatedAt(LocalDateTime.now());
        return toResponse(notificationPreferenceRepository.save(preference));
    }

    @Transactional
    public PreferenceResponse toggleEmailNotifications(String userId) {
        NotificationPreference preference = getOrCreatePreference(userId);
        preference.setEmailEnabled(!Boolean.TRUE.equals(preference.getEmailEnabled()));
        preference.setUpdatedAt(LocalDateTime.now());
        return toResponse(notificationPreferenceRepository.save(preference));
    }

    @Transactional
    public PreferenceResponse unsubscribeFromChannel(String userId, String channel) {
        NotificationPreference preference = getOrCreatePreference(userId);
        if (preference.getUnsubscribedChannels() == null) {
            preference.setUnsubscribedChannels(new ArrayList<>());
        }

        if (!preference.getUnsubscribedChannels().contains(channel)) {
            preference.getUnsubscribedChannels().add(channel);
        }

        preference.setUpdatedAt(LocalDateTime.now());
        return toResponse(notificationPreferenceRepository.save(preference));
    }

    private NotificationPreference getOrCreatePreference(String userId) {
        return notificationPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> notificationPreferenceRepository.save(createDefaultPreference(userId)));
    }

    private NotificationPreference createDefaultPreference(String userId) {
        NotificationPreference preference = new NotificationPreference();
        preference.setUserId(userId);
        preference.setEmailEnabled(true);
        preference.setSmsEnabled(false);
        preference.setPushEnabled(true);
        preference.setDndEnabled(false);
        preference.setDigestFrequency(NotificationFrequency.REAL_TIME);
        preference.setUnsubscribedChannels(new ArrayList<>());
        preference.setCreatedAt(LocalDateTime.now());
        preference.setUpdatedAt(LocalDateTime.now());
        return preference;
    }

    private PreferenceResponse toResponse(NotificationPreference preference) {
        PreferenceResponse response = new PreferenceResponse();
        response.setPreferenceId(preference.getId());
        response.setEmailEnabled(preference.getEmailEnabled());
        response.setSmsEnabled(preference.getSmsEnabled());
        response.setPushEnabled(preference.getPushEnabled());
        response.setDndEnabled(preference.getDndEnabled());
        response.setDndStartTime(preference.getDndStartTime() != null ? preference.getDndStartTime().toString() : null);
        response.setDndEndTime(preference.getDndEndTime() != null ? preference.getDndEndTime().toString() : null);
        response.setDigestFrequency(preference.getDigestFrequency() != null ? preference.getDigestFrequency().name() : null);
        return response;
    }

    private Boolean defaultIfNull(Boolean value, Boolean fallback) {
        return value != null ? value : fallback;
    }
}
