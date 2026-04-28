package com.eduplatform.admin.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "course_reports")
public class CourseReport {
    @Id
    private String id;

    @Indexed
    private String courseId;
    private String courseName;

    @Indexed
    private String instructorId;

    @Indexed
    private String reporterId;

    private CourseReportReason reason;

    private String description;
    private List<String> evidenceUrls;

    @Indexed
    private ReportStatus status;

    // Resolution
    private String reviewedBy;
    private String reviewNotes;
    private CourseReportAction actionTaken;

    private LocalDateTime reportedAt;
    private LocalDateTime reviewedAt;

    @Indexed
    private String tenantId;
}

enum CourseReportReason {
    PLAGIARISM,
    HARMFUL_CONTENT,
    MISLEADING_INFORMATION,
    POOR_QUALITY,
    COPYRIGHT_INFRINGEMENT,
    OTHER
}

enum CourseReportAction {
    APPROVED,
    REJECTED,
    REMOVED,
    FLAGGED_FOR_REVIEW,
    REQUESTED_REVISION,
    NONE
}