package com.eduplatform.video.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "videos")
@CompoundIndex(name = "courseId_tenantId_idx", def = "{'courseId': 1, 'tenantId': 1}")
public class Video {

    @Id
    private String id;

    @Indexed(unique = true)
    private String videoId; // vid_XXXXX

    private String title;

    private String description;

    @Indexed
    private String courseId;

    @Indexed
    private String chapterId; // For organization

    @Indexed
    private String instructorId;

    private String thumbnailUrl;

    private String originalFileName;

    private Long fileSizeBytes;

    private Integer durationSeconds;

    @Indexed
    private String status; // UPLOADING, PROCESSING, READY, FAILED, ARCHIVED

    private String quality360Url;      // S3 presigned URL
    private String quality720Url;      // S3 presigned URL
    private String quality1080Url;     // S3 presigned URL
    private String qualityAutoUrl;     // S3 presigned URL (adaptive bitrate)

    private String hlsManifestUrl;     // HLS .m3u8 playlist
    private String dashManifestUrl;    // DASH .mpd manifest

    private List<String> subtitleLanguages; // en, hi, es

    private Map<String, String> subtitleUrls; // lang -> S3 URL

    private Boolean isPublished;

    private Boolean allowDownload;

    private Boolean requiresAuth;

    @Indexed
    private LocalDateTime uploadedAt;

    @Indexed
    private LocalDateTime processedAt;

    @Indexed
    private LocalDateTime publishedAt;

    private String s3BucketName;

    private String s3ObjectKey; // Full path in bucket

    private Long viewCount;

    private Double avgWatchTime; // Average minutes watched

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime updatedAt;

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Is video ready to stream
     */
    public boolean isReady() {
        return "READY".equals(status) && qualityAutoUrl != null;
    }

    /**
     * Get streaming URL (HLS preferred)
     */
    public String getStreamingUrl() {
        if (hlsManifestUrl != null) {
            return hlsManifestUrl;
        }
        return qualityAutoUrl;
    }
}