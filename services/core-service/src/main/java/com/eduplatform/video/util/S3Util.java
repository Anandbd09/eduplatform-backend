// FILE 30: S3Util.java
package com.eduplatform.video.util;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class S3Util {

    public static String generatePresignedUrl(String bucket, String key, String region) {
        String url = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
        log.info("Presigned URL generated: {}", url);
        return url;
    }

    public static String getObjectKey(String videoId, String quality) {
        return "videos/" + videoId + "/" + quality + "p/video.mp4";
    }
}