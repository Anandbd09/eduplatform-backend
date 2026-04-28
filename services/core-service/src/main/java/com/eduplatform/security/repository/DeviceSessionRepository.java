// FILE 10: DeviceSessionRepository.java
package com.eduplatform.security.repository;

import com.eduplatform.security.model.DeviceSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceSessionRepository extends MongoRepository<DeviceSession, String> {

    List<DeviceSession> findByUserIdAndStatusAndTenantId(String userId, String status, String tenantId);

    Optional<DeviceSession> findByUserIdAndDeviceIdAndTenantId(String userId, String deviceId, String tenantId);

    Page<DeviceSession> findByUserIdAndTenantId(String userId, String tenantId, Pageable pageable);
}