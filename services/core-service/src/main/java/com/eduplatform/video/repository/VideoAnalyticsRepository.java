// FILE 12: VideoAnalyticsRepository.java
package com.eduplatform.video.repository;

import com.eduplatform.video.model.VideoAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VideoAnalyticsRepository extends MongoRepository<VideoAnalytics, String> {

    Optional<VideoAnalytics> findByVideoIdAndTenantId(String videoId, String tenantId);
}