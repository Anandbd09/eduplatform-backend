// FILE 10: VideoTranscodeRepository.java
package com.eduplatform.video.repository;

import com.eduplatform.video.model.VideoTranscode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoTranscodeRepository extends MongoRepository<VideoTranscode, String> {

    Optional<VideoTranscode> findByVideoIdAndTenantId(String videoId, String tenantId);

    List<VideoTranscode> findByStatusAndTenantId(String status, String tenantId);
}