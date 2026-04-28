package com.eduplatform.qa.dto;

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
public class QuestionResponse {

    private String id;
    private String courseId;
    private String userId;
    private String userName;
    private String userEmail;
    private String userAvatar;

    private String title;
    private String content;

    private String status;

    private Integer views;
    private Integer upVotes;
    private Integer downVotes;
    private Integer netVotes;
    private Integer answerCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Boolean isAnswered;
    private Boolean hasBestAnswer;
    private String bestAnswerId;

    private List<String> tags;
}