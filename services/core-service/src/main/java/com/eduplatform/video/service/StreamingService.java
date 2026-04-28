package com.eduplatform.video.service;

import com.eduplatform.video.exception.VideoException;
import com.eduplatform.video.model.Video;
import com.eduplatform.video.util.HLSGenerator;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StreamingService {

    private final HLSGenerator hlsGenerator = new HLSGenerator();

    /**
     * GENERATE HLS MANIFEST
     */
    public String generateHLSManifest(Video video) {
        try {
            validateVideoForAdaptiveStreaming(video);
            String hlsUrl = hlsGenerator.generateM3U8(video);
            log.info("HLS manifest generated: videoId={}", video.getVideoId());
            return hlsUrl;
        } catch (Exception e) {
            log.error("Error generating HLS manifest: videoId={}", getVideoId(video), e);
            throw new VideoException("Failed to generate HLS manifest");
        }
    }

    /**
     * GENERATE DASH MANIFEST
     */
    public String generateDASHManifest(Video video) {
        try {
            validateVideoForAdaptiveStreaming(video);
            String dashUrl = hlsGenerator.generateMPD(video);
            log.info("DASH manifest generated: videoId={}", video.getVideoId());
            return dashUrl;
        } catch (Exception e) {
            log.error("Error generating DASH manifest: videoId={}", getVideoId(video), e);
            throw new VideoException("Failed to generate DASH manifest");
        }
    }

    private void validateVideoForAdaptiveStreaming(Video video) {
        if (video == null) {
            throw new VideoException("Video is required for manifest generation");
        }
        if (isBlank(video.getVideoId())) {
            throw new VideoException("Video ID is required for manifest generation");
        }
        if (isBlank(video.getQuality360Url())
                || isBlank(video.getQuality720Url())
                || isBlank(video.getQuality1080Url())) {
            throw new VideoException("All transcoded quality URLs must be available before manifest generation");
        }
    }

    private String getVideoId(Video video) {
        return video != null ? video.getVideoId() : "unknown";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
