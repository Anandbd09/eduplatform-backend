package com.eduplatform.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private String id;
    private String courseId;
    private String userId;
    private String userName;
    private String userEmail;
    private String userAvatar;

    private Integer rating;
    private String title;
    private String content;

    private Integer helpfulCount;
    private Integer unHelpfulCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String status;
    private Boolean isVerifiedPurchase;

    private List<String> tags;

    private Long ageInDays;
    private Boolean canEdit;
}