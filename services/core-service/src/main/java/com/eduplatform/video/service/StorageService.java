package com.eduplatform.video.service;

import com.eduplatform.video.exception.VideoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.time.Instant;

@Slf4j
@Service
public class StorageService {

    @Value("${aws.s3.bucket:eduplatform-videos}")
    private String bucketName;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Value("${aws.access.key:}")
    private String accessKey;

    @Value("${aws.secret.key:}")
    private String secretKey;

    /**
     * GENERATE PRESIGNED UPLOAD URL
     */
    public String generatePresignedUploadUrl(String videoId, String fileName) {
        try {
            String key = "videos/" + videoId + "/" + fileName;

            // In production: use AWS SDK to generate real presigned URL
            // s3Client.generatePresignedUrl(GetPresignedUrlRequest)

            // Mock presigned URL
            String mockPresignedUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/"
                    + key + "?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=3600";

            log.info("Presigned URL generated for upload: videoId={}", videoId);
            return mockPresignedUrl;

        } catch (Exception e) {
            log.error("Error generating presigned URL", e);
            throw new VideoException("Failed to generate presigned URL");
        }
    }

    /**
     * GET OBJECT KEY
     */
    public String getObjectKey(String videoId) {
        return "videos/" + videoId;
    }

    /**
     * GET BUCKET NAME
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * DELETE FROM S3
     */
    public void deleteFromS3(String objectKey) {
        try {
            // In production: use AWS SDK
            // s3Client.deleteObject(DeleteObjectRequest)
            log.info("Object deleted from S3: key={}", objectKey);
        } catch (Exception e) {
            log.error("Error deleting from S3", e);
            throw new VideoException("Failed to delete from S3");
        }
    }
}