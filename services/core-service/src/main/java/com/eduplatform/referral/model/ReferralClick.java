package com.eduplatform.referral.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "referral_clicks")
@CompoundIndex(name = "referralCode_timestamp_idx", def = "{'referralCode': 1, 'clickedAt': 1}")
public class ReferralClick {

    @Id
    private String id;

    @Indexed
    private String referralCode;

    @Indexed
    private String instructorId;

    private String visitorEmail; // Email of person who clicked (if provided)

    private String visitorIp;

    private String visitorUserAgent; // Browser info

    private String visitorDeviceType; // MOBILE, TABLET, DESKTOP

    private String visitorCountry;

    @Indexed
    private LocalDateTime clickedAt;

    @Indexed
    private LocalDateTime expiresAt; // Click attribution window (30 days)

    private String clickedFromPage; // URL they came from

    private LocalDateTime convertedAt; // When they purchased (if they did)

    @Indexed
    private String status; // PENDING, CONVERTED, EXPIRED (after 30 days)

    private String courseId; // Course they purchased (if converted)

    private String orderId; // Order ID (if converted)

    private Double orderAmount; // Purchase amount (if converted)

    @Indexed
    private String tenantId;

    private Long version = 0L;

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
