package com.eduplatform.video.service;

import com.eduplatform.video.model.*;
import com.eduplatform.video.repository.*;
import com.eduplatform.video.exception.VideoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class TranscodingService {

    @Autowired
    private VideoTranscodeRepository transcodeRepository;

    @Autowired
    private VideoRepository videoRepository;

    /**
     * QUEUE TRANSCODING JOBS FOR ALL QUALITIES
     */
    public void queueTranscodingJob(String videoId, String tenantId) {
        try {
            String[] qualities = {"360", "720", "1080"};

            for (String quality : qualities) {
                VideoTranscode transcode = VideoTranscode.builder()
                        .id(UUID.randomUUID().toString())
                        .videoId(videoId)
                        .quality(quality)
                        .status("QUEUED")
                        .progressPercentage(0.0)
                        .createdAt(LocalDateTime.now())
                        .tenantId(tenantId)
                        .build();

                transcodeRepository.save(transcode);
                log.info("Transcode job queued: videoId={}, quality={}p", videoId, quality);
            }

            // In production, this would publish to RabbitMQ/SQS
            // rabbitTemplate.convertAndSend("transcode-queue", new TranscodeJob(videoId, tenantId));

        } catch (Exception e) {
            log.error("Error queueing transcoding job", e);
            throw new VideoException("Failed to queue transcode job");
        }
    }

    /**
     * UPDATE TRANSCODING STATUS
     */
    public void updateTranscodingStatus(String videoId, String quality, String status,
                                        String outputUrl, String tenantId) {
        try {
            var transcode = transcodeRepository.findByVideoIdAndTenantId(videoId, tenantId);

            if (transcode.isPresent()) {
                VideoTranscode t = transcode.get();
                t.setStatus(status);
                t.setS3OutputUrl(outputUrl);

                if ("COMPLETED".equals(status)) {
                    t.setCompletedAt(LocalDateTime.now());
                    t.setProgressPercentage(100.0);
                } else if ("PROCESSING".equals(status)) {
                    t.setStartedAt(LocalDateTime.now());
                }

                transcodeRepository.save(t);
                log.info("Transcode status updated: videoId={}, quality={}, status={}",
                        videoId, quality, status);
            }
        } catch (Exception e) {
            log.error("Error updating transcode status", e);
            throw new VideoException("Failed to update transcode status");
        }
    }

    /**
     * GET TRANSCODE JOB STATUS
     */
    public VideoTranscode getTranscodeStatus(String videoId, String tenantId) {
        try {
            var transcode = transcodeRepository.findByVideoIdAndTenantId(videoId, tenantId);
            return transcode.orElse(null);
        } catch (Exception e) {
            log.error("Error getting transcode status", e);
            throw new VideoException("Failed to get transcode status");
        }
    }
}