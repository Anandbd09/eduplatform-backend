// FILE 7: VideoRepository.java
package com.eduplatform.video.repository;

import com.eduplatform.video.model.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends MongoRepository<Video, String> {

    Optional<Video> findByVideoIdAndTenantId(String videoId, String tenantId);

    Page<Video> findByCourseIdAndTenantId(String courseId, String tenantId, Pageable pageable);

    Page<Video> findByStatusAndTenantId(String status, String tenantId, Pageable pageable);

    Page<Video> findByInstructorIdAndTenantId(String instructorId, String tenantId, Pageable pageable);

    List<Video> findByChapterIdAndTenantId(String chapterId, String tenantId);

    @Query("{ 'isPublished': true, 'status': 'READY', 'tenantId': ?0 }")
    List<Video> findPublishedVideos(String tenantId);

    Long countByStatusAndTenantId(String status, String tenantId);
}
