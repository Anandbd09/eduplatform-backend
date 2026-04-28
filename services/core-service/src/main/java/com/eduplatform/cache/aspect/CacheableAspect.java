package com.eduplatform.cache.aspect;

import com.eduplatform.cache.service.CachingService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class CacheableAspect {

    @Autowired
    private CachingService cachingService;

    /**
     * INTERCEPT METHODS WITH @Cacheable ANNOTATION
     */
    @Around("@annotation(cacheable)")
    public Object cacheMethod(ProceedingJoinPoint joinPoint, Cacheable cacheable) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (args.length < 2 || args[0] == null || args[1] == null) {
            return joinPoint.proceed();
        }

        if (joinPoint.getTarget() instanceof CachingService) {
            return joinPoint.proceed();
        }

        String cacheKey = args[0].toString();
        String tenantId = args[1].toString();

        try {
            Object cachedValue = cachingService.getCachedValue(cacheKey, tenantId);
            if (cachedValue != null) {
                log.debug("Cache hit: {}", cacheKey);
                return cachedValue;
            }
        } catch (Exception e) {
            log.warn("Failed to read cache for key={}", cacheKey, e);
        }

        Object result = joinPoint.proceed();

        if (result != null) {
            try {
                String[] cacheNames = cacheable.cacheNames().length > 0 ? cacheable.cacheNames() : cacheable.value();
                String cacheType = cacheNames.length > 0 ? cacheNames[0] : joinPoint.getSignature().getName().toUpperCase();
                cachingService.putInCache(cacheKey, result, cacheType.toUpperCase(), 3600, tenantId);
            } catch (Exception e) {
                log.warn("Failed to write cache for key={}", cacheKey, e);
            }
        }

        return result;
    }
}
