package com.eduplatform.review.model;

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
@Document(collection = "review_helpful")
@CompoundIndex(name = "reviewId_userId_idx", def = "{'reviewId': 1, 'userId': 1}", unique = true)
public class ReviewHelpful {

    @Id
    private String id;

    @Indexed
    private String reviewId;

    @Indexed
    private String userId;

    private Boolean isHelpful; // true = helpful, false = not helpful

    @Indexed
    private LocalDateTime markedAt;
}