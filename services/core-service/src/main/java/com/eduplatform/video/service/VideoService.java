package com.eduplatform.video.service;

import com.eduplatform.video.model.*;
import com.eduplatform.video.repository.*;
import com.eduplatform.video.dto.*;
import com.eduplatform.video.exception.VideoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class VideoService {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private VideoChapterRepository chapterRepository;

    @Autowired
    private VideoPlaybackRepository playbackRepository;

    @Autowired
    private VideoTranscodeRepository transcodeRepository;

    @Autowired
    private VideoSubtitleRepository subtitleRepository;

    @Autowired
    private VideoAnalyticsRepository analyticsRepository;

    @Autowired
    private TranscodingService transcodingService;

    @Autowired
    private PlaybackService playbackService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private StreamingService streamingService;

    /**
     * UPLOAD VIDEO - INITIATE UPLOAD PROCESS
     */
    public VideoUploadResponse uploadVideo(VideoRequest request, String instructorId, String tenantId) {
        try {
            // Validate request
            if (request.getTitle() == null || request.getTitle().isEmpty()) {
                throw new VideoException("Video title is required");
            }

            if (request.getCourseId() == null) {
                throw new VideoException("Course ID is required");
            }

            // Generate unique video ID
            String videoId = "vid_" + UUID.randomUUID().toString().substring(0, 12);

            // Create video entity
            Video video = Video.builder()
                    .id(UUID.randomUUID().toString())
                    .videoId(videoId)
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .courseId(request.getCourseId())
                    .chapterId(request.getChapterId())
                    .instructorId(instructorId)
                    .status("UPLOADING")
                    .isPublished(false)
                    .allowDownload(request.getAllowDownload() != null ? request.getAllowDownload() : false)
                    .requiresAuth(request.getRequiresAuth() != null ? request.getRequiresAuth() : true)
                    .uploadedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();

            // Save video
            Video savedVideo = videoRepository.save(video);

            // Generate S3 presigned upload URL
            String s3PresignedUrl = storageService.generatePresignedUploadUrl(
                    videoId,
                    request.getFileName()
            );

            // Create analytics record
            VideoAnalytics analytics = VideoAnalytics.builder()
                    .id(UUID.randomUUID().toString())
                    .videoId(videoId)
                    .totalViews(0L)
                    .uniqueViewers(0L)
                    .avgWatchTimeSeconds(0.0)
                    .avgCompletionPercentage(0.0)
                    .totalPlayCount(0L)
                    .engagementScore(0.0)
                    .createdAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();
            analyticsRepository.save(analytics);

            log.info("Video upload initiated: videoId={}, courseId={}, instructor={}",
                    videoId, request.getCourseId(), instructorId);

            return VideoUploadResponse.builder()
                    .videoId(videoId)
                    .status("UPLOADING")
                    .presignedUrl(s3PresignedUrl)
                    .bucket(storageService.getBucketName())
                    .key(storageService.getObjectKey(videoId))
                    .build();

        } catch (VideoException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error uploading video", e);
            throw new VideoException("Failed to upload video: " + e.getMessage());
        }
    }

    /**
     * GET VIDEO METADATA
     */
    public VideoResponse getVideo(String videoId, String tenantId) {
        try {
            Optional<Video> video = videoRepository.findByVideoIdAndTenantId(videoId, tenantId);
            if (video.isEmpty()) {
                throw new VideoException("Video not found");
            }
            return convertToResponse(video.get());
        } catch (VideoException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching video", e);
            throw new VideoException("Failed to fetch video");
        }
    }

    /**
     * GET STREAMING URL (HLS/DASH)
     */
    public StreamingManifestResponse getStreamingUrl(String videoId, String tenantId) {
        try {
            Optional<Video> video = videoRepository.findByVideoIdAndTenantId(videoId, tenantId);
            if (video.isEmpty()) {
                throw new VideoException("Video not found");
            }

            Video v = video.get();
            if (!v.isReady()) {
                throw new VideoException("Video is not ready for streaming");
            }

            return StreamingManifestResponse.builder()
                    .videoId(videoId)
                    .hlsUrl(v.getHlsManifestUrl())
                    .dashUrl(v.getDashManifestUrl())
                    .progressiveUrl(v.getQualityAutoUrl())
                    .subtitles(v.getSubtitleUrls())
                    .duration(v.getDurationSeconds())
                    .build();

        } catch (VideoException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting streaming URL", e);
            throw new VideoException("Failed to get streaming URL");
        }
    }

    /**
     * UPDATE PLAYBACK PROGRESS
     */
    public PlaybackResponse updatePlayback(String videoId, PlaybackRequest request, String userId, String tenantId) {
        try {
            // Get or create playback record
            Optional<VideoPlayback> existing = playbackRepository
                    .findByUserIdAndVideoIdAndTenantId(userId, videoId, tenantId);

            VideoPlayback playback;
            if (existing.isPresent()) {
                playback = existing.get();
            } else {
                playback = VideoPlayback.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(userId)
                        .videoId(videoId)
                        .courseId(request.getCourseId())
                        .playCount(0)
                        .totalWatchedSeconds(0L)
                        .createdAt(LocalDateTime.now())
                        .tenantId(tenantId)
                        .build();
            }

            // Update playback info
            playback.setCurrentPositionSeconds(request.getCurrentPositionSeconds());
            playback.setTotalWatchedSeconds(playback.getTotalWatchedSeconds() + request.getWatchedSeconds());
            playback.setDeviceType(request.getDeviceType());
            playback.setPlayerQuality(request.getQuality());
            playback.setPlaybackSpeed(request.getPlaybackSpeed());
            playback.setLastPlayedAt(LocalDateTime.now());

            // Calculate completion percentage
            Optional<Video> video = videoRepository.findByVideoIdAndTenantId(videoId, tenantId);
            if (video.isPresent() && video.get().getDurationSeconds() != null) {
                Double duration = video.get().getDurationSeconds().doubleValue();
                Double completion = (playback.getTotalWatchedSeconds() / duration) * 100;
                playback.setCompletionPercentage(Math.min(completion, 100.0));

                // Mark as completed if >= 90%
                if (completion >= 90.0 && playback.getCompletedAt() == null) {
                    playback.setCompletedAt(LocalDateTime.now());
                }
            }

            // Save playback
            VideoPlayback saved = playbackRepository.save(playback);

            // Update analytics
            updateAnalytics(videoId, tenantId);

            log.info("Playback updated: videoId={}, userId={}, position={}",
                    videoId, userId, request.getCurrentPositionSeconds());

            return PlaybackResponse.builder()
                    .videoId(videoId)
                    .userId(userId)
                    .currentPositionSeconds(saved.getCurrentPositionSeconds())
                    .completionPercentage(saved.getCompletionPercentage())
                    .isCompleted(saved.isCompleted())
                    .lastPlayedAt(saved.getLastPlayedAt())
                    .build();

        } catch (Exception e) {
            log.error("Error updating playback", e);
            throw new VideoException("Failed to update playback");
        }
    }

    /**
     * GET USER PLAYBACK HISTORY
     */
    public Page<PlaybackResponse> getUserPlaybacks(String userId, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("lastPlayedAt").descending());

            Page<VideoPlayback> playbacks = playbackRepository.findByUserIdAndTenantId(userId, tenantId, pageable);
            return playbacks.map(this::convertPlaybackToResponse);

        } catch (Exception e) {
            log.error("Error fetching user playbacks", e);
            throw new VideoException("Failed to fetch playback history");
        }
    }

    /**
     * GET VIDEO ANALYTICS
     */
    public Map<String, Object> getAnalytics(String videoId, String tenantId) {
        try {
            Optional<VideoAnalytics> analytics = analyticsRepository.findByVideoIdAndTenantId(videoId, tenantId);

            if (analytics.isEmpty()) {
                Map<String, Object> emptyAnalytics = new LinkedHashMap<>();
                emptyAnalytics.put("videoId", videoId);
                emptyAnalytics.put("totalViews", 0L);
                emptyAnalytics.put("uniqueViewers", 0L);
                emptyAnalytics.put("avgWatchTime", 0.0);
                emptyAnalytics.put("completionRate", 0.0);
                emptyAnalytics.put("engagementScore", 0.0);
                return emptyAnalytics;
            }

            VideoAnalytics a = analytics.get();
            Map<String, Object> analyticsResponse = new LinkedHashMap<>();
            analyticsResponse.put("videoId", videoId);
            analyticsResponse.put("totalViews", a.getTotalViews());
            analyticsResponse.put("uniqueViewers", a.getUniqueViewers());
            analyticsResponse.put("avgWatchTime", a.getAvgWatchTimeSeconds());
            analyticsResponse.put("completionRate", a.getAvgCompletionPercentage());
            analyticsResponse.put("engagementScore", a.getEngagementScore());
            analyticsResponse.put("quality360Views", a.getQuality360Views());
            analyticsResponse.put("quality720Views", a.getQuality720Views());
            analyticsResponse.put("quality1080Views", a.getQuality1080Views());
            analyticsResponse.put("topCountry", a.getTopCountry());
            analyticsResponse.put("topDevice", a.getTopDeviceType());
            return analyticsResponse;

        } catch (Exception e) {
            log.error("Error fetching analytics", e);
            throw new VideoException("Failed to fetch analytics");
        }
    }

    /**
     * PUBLISH VIDEO
     */
    public VideoResponse publishVideo(String videoId, String tenantId) {
        try {
            Optional<Video> video = videoRepository.findByVideoIdAndTenantId(videoId, tenantId);
            if (video.isEmpty()) {
                throw new VideoException("Video not found");
            }

            Video v = video.get();
            if (!v.isReady()) {
                throw new VideoException("Video is not ready for publishing (still processing)");
            }

            v.setIsPublished(true);
            v.setPublishedAt(LocalDateTime.now());
            v.setUpdatedAt(LocalDateTime.now());

            Video updated = videoRepository.save(v);
            log.info("Video published: videoId={}", videoId);

            return convertToResponse(updated);

        } catch (VideoException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error publishing video", e);
            throw new VideoException("Failed to publish video");
        }
    }

    /**
     * DELETE VIDEO
     */
    public void deleteVideo(String videoId, String tenantId) {
        try {
            Optional<Video> video = videoRepository.findByVideoIdAndTenantId(videoId, tenantId);
            if (video.isEmpty()) {
                throw new VideoException("Video not found");
            }

            Video v = video.get();

            // Delete from S3
            storageService.deleteFromS3(v.getS3ObjectKey());

            // Mark as archived
            v.setStatus("ARCHIVED");
            v.setUpdatedAt(LocalDateTime.now());
            videoRepository.save(v);

            log.info("Video deleted: videoId={}", videoId);

        } catch (VideoException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting video", e);
            throw new VideoException("Failed to delete video");
        }
    }

    /**
     * GET VIDEOS IN COURSE
     */
    public Page<VideoResponse> getCourseVideos(String courseId, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Video> videos = videoRepository.findByCourseIdAndTenantId(courseId, tenantId, pageable);
            return videos.map(this::convertToResponse);

        } catch (Exception e) {
            log.error("Error fetching course videos", e);
            throw new VideoException("Failed to fetch videos");
        }
    }

    /**
     * CREATE CHAPTER
     */
    public VideoChapter createChapter(String courseId, String title, Integer chapterNumber, String tenantId) {
        try {
            VideoChapter chapter = VideoChapter.builder()
                    .id(UUID.randomUUID().toString())
                    .courseId(courseId)
                    .chapterTitle(title)
                    .chapterNumber(chapterNumber)
                    .videoIds(new ArrayList<>())
                    .totalDurationSeconds(0L)
                    .createdAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();

            VideoChapter saved = chapterRepository.save(chapter);
            log.info("Chapter created: courseId={}, chapterNumber={}", courseId, chapterNumber);
            return saved;

        } catch (Exception e) {
            log.error("Error creating chapter", e);
            throw new VideoException("Failed to create chapter");
        }
    }

    /**
     * ADD VIDEO TO CHAPTER
     */
    public void addVideoToChapter(String chapterId, String videoId, String tenantId) {
        try {
            Optional<VideoChapter> chapter = chapterRepository.findById(chapterId);
            if (chapter.isEmpty()) {
                throw new VideoException("Chapter not found");
            }

            VideoChapter c = chapter.get();
            if (c.getVideoIds() == null) {
                c.setVideoIds(new ArrayList<>());
            }

            if (!c.getVideoIds().contains(videoId)) {
                c.getVideoIds().add(videoId);
                chapterRepository.save(c);
                log.info("Video added to chapter: chapterId={}, videoId={}", chapterId, videoId);
            }

        } catch (VideoException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error adding video to chapter", e);
            throw new VideoException("Failed to add video to chapter");
        }
    }

    /**
     * UPLOAD SUBTITLE
     */
    public SubtitleResponse uploadSubtitle(String videoId, String language, String fileUrl, String tenantId) {
        try {
            VideoSubtitle subtitle = VideoSubtitle.builder()
                    .id(UUID.randomUUID().toString())
                    .videoId(videoId)
                    .language(language)
                    .languageName(getLanguageName(language))
                    .subtitleUrl(fileUrl)
                    .status("READY")
                    .isAutoGenerated(false)
                    .uploadedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();

            VideoSubtitle saved = subtitleRepository.save(subtitle);

            // Update video subtitle list
            Optional<Video> video = videoRepository.findByVideoIdAndTenantId(videoId, tenantId);
            if (video.isPresent()) {
                Video v = video.get();
                if (v.getSubtitleLanguages() == null) {
                    v.setSubtitleLanguages(new ArrayList<>());
                }
                if (!v.getSubtitleLanguages().contains(language)) {
                    v.getSubtitleLanguages().add(language);
                }
                if (v.getSubtitleUrls() == null) {
                    v.setSubtitleUrls(new HashMap<>());
                }
                v.getSubtitleUrls().put(language, fileUrl);
                videoRepository.save(v);
            }

            log.info("Subtitle uploaded: videoId={}, language={}", videoId, language);

            return SubtitleResponse.builder()
                    .videoId(videoId)
                    .language(language)
                    .languageName(getLanguageName(language))
                    .status("READY")
                    .url(fileUrl)
                    .build();

        } catch (Exception e) {
            log.error("Error uploading subtitle", e);
            throw new VideoException("Failed to upload subtitle");
        }
    }

    /**
     * GET SUBTITLES FOR VIDEO
     */
    public List<SubtitleResponse> getSubtitles(String videoId, String tenantId) {
        try {
            List<VideoSubtitle> subtitles = subtitleRepository.findByVideoIdAndTenantId(videoId, tenantId);
            return subtitles.stream()
                    .map(s -> SubtitleResponse.builder()
                            .videoId(videoId)
                            .language(s.getLanguage())
                            .languageName(s.getLanguageName())
                            .status(s.getStatus())
                            .url(s.getSubtitleUrl())
                            .isAutoGenerated(s.getIsAutoGenerated())
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching subtitles", e);
            throw new VideoException("Failed to fetch subtitles");
        }
    }

    /**
     * FINALIZE UPLOAD - TRIGGER TRANSCODING
     */
    public void finalizeUpload(String videoId, Long fileSizeBytes, Integer durationSeconds, String tenantId) {
        try {
            Optional<Video> video = videoRepository.findByVideoIdAndTenantId(videoId, tenantId);
            if (video.isEmpty()) {
                throw new VideoException("Video not found");
            }

            Video v = video.get();
            v.setStatus("PROCESSING");
            v.setFileSizeBytes(fileSizeBytes);
            v.setDurationSeconds(durationSeconds);
            v.setUpdatedAt(LocalDateTime.now());
            videoRepository.save(v);

            // Queue transcoding jobs
            transcodingService.queueTranscodingJob(videoId, tenantId);

            log.info("Video upload finalized, transcoding queued: videoId={}", videoId);

        } catch (VideoException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error finalizing upload", e);
            throw new VideoException("Failed to finalize upload");
        }
    }

    /**
     * UPDATE TRANSCODING STATUS
     */
    public void updateTranscodingStatus(String videoId, String quality, String status, String outputUrl, String tenantId) {
        try {
            // Update transcode record
            transcodingService.updateTranscodingStatus(videoId, quality, status, outputUrl, tenantId);

            Optional<Video> video = videoRepository.findByVideoIdAndTenantId(videoId, tenantId);
            if (video.isPresent()) {
                Video v = video.get();

                // Set quality URLs
                if ("360".equals(quality)) {
                    v.setQuality360Url(outputUrl);
                } else if ("720".equals(quality)) {
                    v.setQuality720Url(outputUrl);
                } else if ("1080".equals(quality)) {
                    v.setQuality1080Url(outputUrl);
                }

                // If all qualities done, generate streaming manifests
                if (v.getQuality360Url() != null && v.getQuality720Url() != null && v.getQuality1080Url() != null) {
                    v.setQualityAutoUrl(v.getQuality1080Url());

                    // Generate HLS and DASH manifests
                    String hlsUrl = streamingService.generateHLSManifest(v);
                    String dashUrl = streamingService.generateDASHManifest(v);

                    v.setHlsManifestUrl(hlsUrl);
                    v.setDashManifestUrl(dashUrl);
                    v.setStatus("READY");
                    v.setProcessedAt(LocalDateTime.now());
                }

                v.setUpdatedAt(LocalDateTime.now());
                videoRepository.save(v);
            }

            log.info("Transcoding status updated: videoId={}, quality={}, status={}", videoId, quality, status);

        } catch (Exception e) {
            log.error("Error updating transcoding status", e);
            throw new VideoException("Failed to update transcoding status");
        }
    }

    /**
     * UPDATE ANALYTICS
     */
    private void updateAnalytics(String videoId, String tenantId) {
        try {
            Optional<VideoAnalytics> analytics = analyticsRepository.findByVideoIdAndTenantId(videoId, tenantId);
            if (analytics.isEmpty()) {
                return;
            }

            VideoAnalytics a = analytics.get();

            // Get all playbacks for this video
            List<VideoPlayback> playbacks = playbackRepository.findAll().stream()
                    .filter(playback -> Objects.equals(playback.getVideoId(), videoId)
                            && Objects.equals(playback.getTenantId(), tenantId))
                    .collect(Collectors.toList());

            // Calculate metrics
            a.setTotalViews((long) playbacks.size());
            a.setUniqueViewers((long) playbacks.stream().map(VideoPlayback::getUserId).distinct().count());
            a.setTotalPlayCount(playbacks.stream().mapToLong(p -> p.getPlayCount() != null ? p.getPlayCount() : 0).sum());

            Double avgWatchTime = playbacks.stream()
                    .mapToDouble(p -> p.getTotalWatchedSeconds() != null ? p.getTotalWatchedSeconds() : 0)
                    .average()
                    .orElse(0.0);
            a.setAvgWatchTimeSeconds(avgWatchTime);

            Double avgCompletion = playbacks.stream()
                    .mapToDouble(p -> p.getCompletionPercentage() != null ? p.getCompletionPercentage() : 0)
                    .average()
                    .orElse(0.0);
            a.setAvgCompletionPercentage(avgCompletion);

            // Calculate engagement score (0-100)
            Double engagementScore = (avgCompletion * 0.6) + ((Math.min(avgWatchTime / 300, 1)) * 40);
            a.setEngagementScore(Math.min(engagementScore, 100.0));

            a.setLastUpdatedAt(LocalDateTime.now());
            analyticsRepository.save(a);

        } catch (Exception e) {
            log.error("Error updating analytics", e);
        }
    }

    /**
     * CONVERT VIDEO TO RESPONSE
     */
    private VideoResponse convertToResponse(Video video) {
        return VideoResponse.builder()
                .videoId(video.getVideoId())
                .title(video.getTitle())
                .description(video.getDescription())
                .courseId(video.getCourseId())
                .instructorId(video.getInstructorId())
                .duration(video.getDurationSeconds())
                .status(video.getStatus())
                .isPublished(video.getIsPublished())
                .isReady(video.isReady())
                .hlsUrl(video.getHlsManifestUrl())
                .dashUrl(video.getDashManifestUrl())
                .subtitleLanguages(video.getSubtitleLanguages())
                .viewCount(video.getViewCount())
                .uploadedAt(video.getUploadedAt())
                .publishedAt(video.getPublishedAt())
                .build();
    }

    /**
     * CONVERT PLAYBACK TO RESPONSE
     */
    private PlaybackResponse convertPlaybackToResponse(VideoPlayback playback) {
        return PlaybackResponse.builder()
                .videoId(playback.getVideoId())
                .userId(playback.getUserId())
                .currentPositionSeconds(playback.getCurrentPositionSeconds())
                .completionPercentage(playback.getCompletionPercentage())
                .isCompleted(playback.isCompleted())
                .lastPlayedAt(playback.getLastPlayedAt())
                .build();
    }

    /**
     * GET LANGUAGE NAME FROM CODE
     */
    private String getLanguageName(String code) {
        Map<String, String> languages = Map.of(
                "en", "English",
                "hi", "Hindi",
                "es", "Spanish",
                "fr", "French",
                "de", "German",
                "pt", "Portuguese",
                "ja", "Japanese",
                "zh", "Chinese"
        );
        return languages.getOrDefault(code, code.toUpperCase());
    }
}
