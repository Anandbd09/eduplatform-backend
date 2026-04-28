package com.eduplatform.gamification.dto;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserRankResponse {
    private String userRank;
    private Integer currentLevel;
    private Integer globalRank;
    private Long totalPoints;
}