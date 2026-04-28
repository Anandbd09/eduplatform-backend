package com.eduplatform.video.config;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@Configuration
@EnableTransactionManagement
public class VideoConfig {

    private final MongoTemplate mongoTemplate;

    public VideoConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initializeIndexes() {
        try {
            log.info("Initializing Video Management indexes...");

            IndexOperations videoIndexOps = mongoTemplate.indexOps("videos");
            ensureIndex(videoIndexOps, new Index()
                    .on("videoId", Sort.Direction.ASC)
                    .unique()
                    .named("videoId_unique"), "videos");
            ensureIndex(videoIndexOps, new Index()
                    .on("courseId", Sort.Direction.ASC)
                    .on("tenantId", Sort.Direction.ASC)
                    .named("courseId_tenantId_idx"), "videos");
            ensureIndex(videoIndexOps, new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"), "videos");
            ensureIndex(videoIndexOps, new Index()
                    .on("uploadedAt", Sort.Direction.DESC)
                    .named("uploadedAt_desc_idx"), "videos");
            log.info("Video indexes checked");

            IndexOperations chapterIndexOps = mongoTemplate.indexOps("video_chapters");
            ensureIndex(chapterIndexOps, new Index()
                    .on("courseId", Sort.Direction.ASC)
                    .named("courseId_idx"), "video_chapters");
            ensureIndex(chapterIndexOps, new Index()
                    .on("chapterNumber", Sort.Direction.ASC)
                    .named("chapterNumber_idx"), "video_chapters");
            ensureIndex(chapterIndexOps, new Index()
                    .on("createdAt", Sort.Direction.DESC)
                    .named("createdAt_desc_idx"), "video_chapters");
            log.info("Video Chapter indexes checked");

            IndexOperations playbackIndexOps = mongoTemplate.indexOps("video_playbacks");
            ensureIndex(playbackIndexOps, new Index()
                    .on("userId", Sort.Direction.ASC)
                    .on("videoId", Sort.Direction.ASC)
                    .on("tenantId", Sort.Direction.ASC)
                    .unique()
                    .named("userId_videoId_tenantId_unique"), "video_playbacks");
            ensureIndex(playbackIndexOps, new Index()
                    .on("lastPlayedAt", Sort.Direction.DESC)
                    .named("lastPlayedAt_desc_idx"), "video_playbacks");
            ensureIndex(playbackIndexOps, new Index()
                    .on("completedAt", Sort.Direction.DESC)
                    .named("completedAt_desc_idx"), "video_playbacks");
            ensureIndex(playbackIndexOps, new Index()
                    .on("completionPercentage", Sort.Direction.DESC)
                    .named("completionPercentage_desc_idx"), "video_playbacks");
            log.info("Video Playback indexes checked");

            IndexOperations transcodeIndexOps = mongoTemplate.indexOps("video_transcodes");
            ensureIndex(transcodeIndexOps, new Index()
                    .on("videoId", Sort.Direction.ASC)
                    .unique()
                    .named("videoId_unique"), "video_transcodes");
            ensureIndex(transcodeIndexOps, new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"), "video_transcodes");
            ensureIndex(transcodeIndexOps, new Index()
                    .on("completedAt", Sort.Direction.DESC)
                    .named("completedAt_desc_idx"), "video_transcodes");
            log.info("Video Transcode indexes checked");

            IndexOperations subtitleIndexOps = mongoTemplate.indexOps("video_subtitles");
            ensureIndex(subtitleIndexOps, new Index()
                    .on("videoId", Sort.Direction.ASC)
                    .named("videoId_idx"), "video_subtitles");
            ensureIndex(subtitleIndexOps, new Index()
                    .on("language", Sort.Direction.ASC)
                    .named("language_idx"), "video_subtitles");
            ensureIndex(subtitleIndexOps, new Index()
                    .on("status", Sort.Direction.ASC)
                    .named("status_idx"), "video_subtitles");
            log.info("Video Subtitle indexes checked");

            IndexOperations analyticsIndexOps = mongoTemplate.indexOps("video_analytics");
            ensureIndex(analyticsIndexOps, new Index()
                    .on("videoId", Sort.Direction.ASC)
                    .unique()
                    .named("videoId_unique"), "video_analytics");
            log.info("Video Analytics indexes checked");

            log.info("Video Management index initialization complete");
        } catch (Exception e) {
            log.error("Error initializing video indexes", e);
            throw new RuntimeException("Failed to initialize video indexes", e);
        }
    }

    private void ensureIndex(IndexOperations indexOperations, Index index, String collectionName) {
        try {
            indexOperations.ensureIndex(index);
        } catch (Exception e) {
            if (isExistingIndexConflict(e)) {
                log.warn("Skipping conflicting existing index on collection '{}': {}", collectionName, e.getMessage());
                return;
            }
            throw e;
        }
    }

    private boolean isExistingIndexConflict(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && (message.contains("IndexOptionsConflict")
                    || message.contains("already exists with a different name"))) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
