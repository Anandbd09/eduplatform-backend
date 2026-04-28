package com.eduplatform.admin.repository;

import com.eduplatform.admin.model.UserReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserReportRepository extends MongoRepository<UserReport, String> {
    List<UserReport> findByReportedUserId(String userId);

    List<UserReport> findByStatusOrderByReportedAtDesc(String status);

    List<UserReport> findByReportedUserIdAndStatus(String userId, String status);

    long countByReportedUserIdAndStatus(String userId, String status);
}