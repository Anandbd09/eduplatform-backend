package com.eduplatform.video.service;

import com.eduplatform.video.model.VideoPlayback;
import com.eduplatform.video.repository.VideoPlaybackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PlaybackService {

    @Autowired
    private VideoPlaybackRepository playbackRepository;

    /**
     * GET USER'S CURRENT PLAYBACK POSITION
     */
    public Optional<VideoPlayback> getCurrentPlayback(String userId, String videoId, String tenantId) {
        return playbackRepository.findByUserIdAndVideoIdAndTenantId(userId, videoId, tenantId);
    }

    /**
     * CHECK IF VIDEO IS COMPLETED BY USER
     */
    public boolean isVideoCompleted(String userId, String videoId, String tenantId) {
        Optional<VideoPlayback> playback = playbackRepository.findByUserIdAndVideoIdAndTenantId(userId, videoId, tenantId);
        return playback.map(VideoPlayback::isCompleted).orElse(false);
    }
}