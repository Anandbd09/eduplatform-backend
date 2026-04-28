package com.eduplatform.qa.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "question_votes")
@CompoundIndex(name = "questionId_userId_idx", def = "{'questionId': 1, 'userId': 1}", unique = true)
public class QuestionVote {

    @Id
    private String id;

    @Indexed
    private String questionId;

    @Indexed
    private String answerId;

    @Indexed
    private String userId;

    private Boolean isUpVote; // true = upvote, false = downvote

    @Indexed
    private LocalDateTime votedAt;

    private String voteType; // QUESTION, ANSWER
}
