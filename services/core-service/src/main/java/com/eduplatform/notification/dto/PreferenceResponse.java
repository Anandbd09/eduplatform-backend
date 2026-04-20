package com.eduplatform.notification.dto;

import lombok.Data;

@Data
public class PreferenceResponse {
    private String preferenceId;
    private Boolean emailEnabled;
    private Boolean smsEnabled;
    private Boolean pushEnabled;
    private Boolean dndEnabled;
    private String dndStartTime;
    private String dndEndTime;
    private String digestFrequency;
}