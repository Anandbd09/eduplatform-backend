// FILE 8: VideoChapterRepository.java
package com.eduplatform.video.repository;

import com.eduplatform.video.model.VideoChapter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VideoChapterRepository extends MongoRepository<VideoChapter, String> {

    List<VideoChapter> findByCourseIdAndTenantId(String courseId, String tenantId);
}