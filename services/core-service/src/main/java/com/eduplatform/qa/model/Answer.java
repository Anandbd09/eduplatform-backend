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
@Document(collection = "answers")
@CompoundIndex(name = "questionId_userId_idx", def = "{'questionId': 1, 'userId': 1, 'tenantId': 1}")
public class Answer {

    @Id
    private String id;

    @Indexed
    private String questionId;

    @Indexed
    private String userId;

    private String userName;
    private String userEmail;
    private String userAvatar;

    private String content;

    private Boolean isBestAnswer = false;

    private Integer upVotes = 0;
    private Integer downVotes = 0;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime updatedAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Get net votes
     */
    public Integer getNetVotes() {
        int up = this.upVotes == null ? 0 : this.upVotes;
        int down = this.downVotes == null ? 0 : this.downVotes;
        return up - down;
    }
}
