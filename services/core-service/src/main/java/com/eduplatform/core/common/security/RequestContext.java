package com.eduplatform.core.common.security;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Locale;

import static java.util.Optional.ofNullable;

@Component
@RequestScope
@Getter
public class RequestContext {

    private String userId;
    private String email;
    private String role;
    private String tenantId;

    /**
     * This is called by an interceptor that reads X-User-Id, X-User-Email, etc. headers
     */
    public void setUserContext(String userId, String email, String role, String tenantId) {
        this.userId = userId;
        this.email = email;
        this.role = normalizeRole(role);
        this.tenantId = ofNullable(tenantId).orElse("default");
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isInstructor() {
        return "INSTRUCTOR".equals(role);
    }

    public boolean isStudent() {
        return "STUDENT".equals(role);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return role;
        }

        String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
        return normalizedRole.startsWith("ROLE_")
                ? normalizedRole.substring("ROLE_".length())
                : normalizedRole;
    }
}
