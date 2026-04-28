package com.eduplatform.security.service;

import com.eduplatform.security.repository.IpWhitelistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
@Service
@Transactional
public class IpWhitelistService {

    @Autowired
    private IpWhitelistRepository ipWhitelistRepository;

    /**
     * CHECK IF IP IS WHITELISTED
     */
    public boolean isIpWhitelisted(String userId, String ipAddress, String tenantId) {
        List<java.util.Optional<?>> whitelisted = new java.util.ArrayList<>();

        // Check if IP exactly matches
        var exact = ipWhitelistRepository.findByUserIdAndIpAddressAndTenantId(userId, ipAddress, tenantId);
        if (exact.isPresent() && exact.get().isStillActive()) {
            return true;
        }

        // Check CIDR blocks
        List<com.eduplatform.security.model.IpWhitelist> all = ipWhitelistRepository
                .findByUserIdAndStatusAndTenantId(userId, "ACTIVE", tenantId);

        return all.stream().anyMatch(w -> isCidrMatch(ipAddress, w.getCidrBlock()));
    }

    /**
     * CHECK CIDR MATCH
     */
    private boolean isCidrMatch(String ip, String cidr) {
        if (cidr == null || cidr.isEmpty()) {
            return false;
        }
        // In production: Implement proper CIDR validation
        return ip.equals(cidr);
    }
}