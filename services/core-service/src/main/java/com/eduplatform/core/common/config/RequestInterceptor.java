package com.eduplatform.core.common.config;

import com.eduplatform.core.common.security.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class RequestInterceptor implements HandlerInterceptor {

    @Autowired
    private RequestContext requestContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String userId = request.getHeader("X-User-Id");
        String email = request.getHeader("X-User-Email");
        String role = request.getHeader("X-User-Role");
        String tenantId = request.getHeader("X-Tenant-Id");

        if (userId != null) {
            requestContext.setUserContext(userId, email, role, tenantId);
            log.debug("Request context set for user: {} ({})", userId, email);
        }

        return true;
    }
}