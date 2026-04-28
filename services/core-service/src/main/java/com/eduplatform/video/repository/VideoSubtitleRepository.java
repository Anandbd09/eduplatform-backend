// FILE 11: VideoSubtitleRepository.java
package com.eduplatform.video.repository;

import com.eduplatform.video.model.VideoSubtitle;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VideoSubtitleRepository extends MongoRepository<VideoSubtitle, String> {

    List<VideoSubtitle> findByVideoIdAndTenantId(String videoId, String tenantId);

    List<VideoSubtitle> findByVideoIdAndLanguageAndTenantId(String videoId, String language, String tenantId);
}