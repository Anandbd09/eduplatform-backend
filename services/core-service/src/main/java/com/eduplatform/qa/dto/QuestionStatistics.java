package com.eduplatform.qa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionStatistics {

    private Long totalQuestions;
    private Long openQuestions;
    private Long answeredQuestions;
    private Long closedQuestions;
}