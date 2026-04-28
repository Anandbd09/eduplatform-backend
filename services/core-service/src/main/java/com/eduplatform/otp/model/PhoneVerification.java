package com.eduplatform.otp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "phone_verifications")
public class PhoneVerification {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    @Indexed
    private String phoneNumber;

    @Indexed
    private String status; // UNVERIFIED, VERIFIED, PENDING

    @Indexed
    private LocalDateTime verifiedAt;

    private Integer verificationCount;

    @Indexed
    private String tenantId;

    private Long version_field = 0L;
}