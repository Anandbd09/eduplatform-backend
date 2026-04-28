package com.eduplatform.qa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerResponse {

    private String id;
    private String questionId;
    private String userId;
    private String userName;
    private String userEmail;
    private String userAvatar;

    private String content;

    private Boolean isBestAnswer;

    private Integer upVotes;
    private Integer downVotes;
    private Integer netVotes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}