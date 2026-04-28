// FILE 9: IpWhitelistRepository.java
package com.eduplatform.security.repository;

import com.eduplatform.security.model.IpWhitelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IpWhitelistRepository extends MongoRepository<IpWhitelist, String> {

    List<IpWhitelist> findByUserIdAndStatusAndTenantId(String userId, String status, String tenantId);

    Optional<IpWhitelist> findByUserIdAndIpAddressAndTenantId(String userId, String ipAddress, String tenantId);

    Page<IpWhitelist> findByUserIdAndTenantId(String userId, String tenantId, Pageable pageable);
}