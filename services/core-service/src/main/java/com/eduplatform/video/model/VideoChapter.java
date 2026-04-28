package com.eduplatform.video.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "video_chapters")
public class VideoChapter {

    @Id
    private String id;

    @Indexed
    private String courseId;

    private String chapterTitle;

    private String chapterDescription;

    @Indexed
    private Integer chapterNumber;

    private List<String> videoIds; // Videos in this chapter

    private Long totalDurationSeconds;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;
}