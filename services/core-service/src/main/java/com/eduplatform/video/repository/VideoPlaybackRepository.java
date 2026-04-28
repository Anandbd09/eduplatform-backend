// FILE 9: VideoPlaybackRepository.java
package com.eduplatform.video.repository;

import com.eduplatform.video.model.VideoPlayback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VideoPlaybackRepository extends MongoRepository<VideoPlayback, String> {

    Optional<VideoPlayback> findByUserIdAndVideoIdAndTenantId(String userId, String videoId, String tenantId);

    Page<VideoPlayback> findByUserIdAndTenantId(String userId, String tenantId, Pageable pageable);

    Page<VideoPlayback> findByCourseIdAndTenantId(String courseId, String tenantId, Pageable pageable);
}