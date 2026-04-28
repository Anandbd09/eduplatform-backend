// FILE 32: HLSGenerator.java
package com.eduplatform.video.util;
import com.eduplatform.video.model.Video;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HLSGenerator {

    public String generateM3U8(Video video) {
        StringBuilder m3u8 = new StringBuilder();
        m3u8.append("#EXTM3U\n");
        m3u8.append("#EXT-X-VERSION:3\n");
        m3u8.append("#EXT-X-TARGETDURATION:10\n");
        m3u8.append("#EXT-X-MEDIA-SEQUENCE:0\n");

        // Add quality variants
        m3u8.append("#EXT-X-STREAM-INF:BANDWIDTH=1280000,RESOLUTION=1920x1080\n");
        m3u8.append(video.getQuality1080Url()).append("\n");
        m3u8.append("#EXT-X-STREAM-INF:BANDWIDTH=750000,RESOLUTION=1280x720\n");
        m3u8.append(video.getQuality720Url()).append("\n");
        m3u8.append("#EXT-X-STREAM-INF:BANDWIDTH=300000,RESOLUTION=640x360\n");
        m3u8.append(video.getQuality360Url()).append("\n");
        m3u8.append("#EXT-X-ENDLIST\n");

        String hlsUrl = "https://cdn.example.com/" + video.getVideoId() + "/playlist.m3u8";
        log.info("HLS manifest generated for video: {}", video.getVideoId());
        return hlsUrl;
    }

    public String generateMPD(Video video) {
        String dashUrl = "https://cdn.example.com/" + video.getVideoId() + "/manifest.mpd";
        log.info("DASH manifest generated for video: {}", video.getVideoId());
        return dashUrl;
    }
}