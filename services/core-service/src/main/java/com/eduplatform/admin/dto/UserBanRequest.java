package com.eduplatform.admin.dto;

import lombok.Data;

@Data
public class UserBanRequest {
    private String userId;
    private String reason;
    private String banType; // TEMPORARY, PERMANENT
    private Integer banDaysTemporary; // For temporary bans
}