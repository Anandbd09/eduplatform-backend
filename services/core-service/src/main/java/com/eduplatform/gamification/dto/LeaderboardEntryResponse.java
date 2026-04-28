package com.eduplatform.gamification.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaderboardEntryResponse {
    private Integer rank;
    private String userId;
    private Long totalPoints;
    private Integer currentLevel;
    private String userRank;
}