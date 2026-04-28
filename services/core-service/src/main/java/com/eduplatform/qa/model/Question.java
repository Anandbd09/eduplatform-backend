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
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "questions")
@CompoundIndex(name = "courseId_userId_idx", def = "{'courseId': 1, 'userId': 1, 'tenantId': 1}")
public class Question {

    @Id
    private String id;

    @Indexed
    private String courseId;

    @Indexed
    private String userId;

    private String userName;
    private String userEmail;
    private String userAvatar;

    private String title;
    private String content;

    private String status; // OPEN, ANSWERED, CLOSED

    private Integer views = 0;
    private Integer upVotes = 0;
    private Integer downVotes = 0;
    private Integer answerCount = 0;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime updatedAt;

    private String bestAnswerId;

    private List<String> tags;

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Increment view count
     */
    public void incrementViews() {
        this.views = (this.views == null ? 0 : this.views) + 1;
    }

    /**
     * Get net votes (upVotes - downVotes)
     */
    public Integer getNetVotes() {
        int up = this.upVotes == null ? 0 : this.upVotes;
        int down = this.downVotes == null ? 0 : this.downVotes;
        return up - down;
    }

    /**
     * Check if question is answered
     */
    public boolean isAnswered() {
        return this.answerCount != null && this.answerCount > 0;
    }

    /**
     * Check if question has best answer
     */
    public boolean hasBestAnswer() {
        return this.bestAnswerId != null && !this.bestAnswerId.isEmpty();
    }

    /**
     * Check if question is closed
     */
    public boolean isClosed() {
        return "CLOSED".equals(this.status);
    }
}
