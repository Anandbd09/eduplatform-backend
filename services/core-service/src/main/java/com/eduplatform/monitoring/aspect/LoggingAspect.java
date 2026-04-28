package com.eduplatform.monitoring.aspect;

import com.eduplatform.monitoring.service.MonitoringService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Autowired
    private MonitoringService monitoringService;

    /**
     * INTERCEPT ALL API CALLS
     */
    @Around("execution(* com.eduplatform.*.controller.*Controller.*(..))")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        jakarta.servlet.http.HttpServletRequest request = attributes.getRequest();
        String endpoint = request.getRequestURI();
        String method = request.getMethod();

        try {
            Object result = joinPoint.proceed();
            long responseTime = System.currentTimeMillis() - startTime;
            monitoringService.logEvent("INFO", "API", "API call successful",
                    null, endpoint, method, 200, responseTime, "default");
            return result;
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            monitoringService.logEvent("ERROR", "API", "API call failed: " + e.getMessage(),
                    null, endpoint, method, 500, responseTime, "default");
            throw e;
        }
    }
}
