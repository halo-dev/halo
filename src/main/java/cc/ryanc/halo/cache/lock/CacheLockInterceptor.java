package cc.ryanc.halo.cache.lock;

import cc.ryanc.halo.cache.StringCacheStore;
import cc.ryanc.halo.exception.FrequentAccessException;
import cc.ryanc.halo.exception.ServiceException;
import cn.hutool.extra.servlet.ServletUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;

/**
 * Interceptor for cache lock annotation.
 *
 * @author johnniang
 * @date 3/28/19
 */
@Slf4j
@Aspect
@Configuration
public class CacheLockInterceptor {

    private final static String CACHE_LOCK_PREFOX = "cache_lock_";

    private final static String CACHE_LOCK_VALUE = "locked";

    private final StringCacheStore cacheStore;

    private final HttpServletRequest httpServletRequest;

    public CacheLockInterceptor(StringCacheStore cacheStore,
                                HttpServletRequest httpServletRequest) {
        this.cacheStore = cacheStore;
        this.httpServletRequest = httpServletRequest;
    }

    @Around("@annotation(cc.ryanc.halo.cache.lock.CacheLock)")
    public Object interceptCacheLock(ProceedingJoinPoint joinPoint) throws Throwable {
        // Get method signature
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        log.debug("Starting locking: [{}]", methodSignature.toString());

        // Get cache lock
        CacheLock cacheLock = methodSignature.getMethod().getAnnotation(CacheLock.class);

        // Build cache lock key
        String cacheLockKey = buildCacheLockKey(cacheLock, joinPoint);

        log.debug("Built lock key: [{}]", cacheLockKey);


        try {
            // Get from cache
            Boolean cacheResult = cacheStore.putIfAbsent(cacheLockKey, CACHE_LOCK_VALUE, cacheLock.expired(), cacheLock.timeUnit());

            if (cacheResult == null) {
                throw new ServiceException("Unknown reason of cache " + cacheLockKey).setErrorData(cacheLockKey);
            }

            if (!cacheResult) {
                throw new FrequentAccessException("Frequent access").setErrorData(cacheLockKey);
            }

            // Proceed the method
            return joinPoint.proceed();
        } finally {
            // Delete the cache
            if (cacheLock.autoDelete()) {
                cacheStore.delete(cacheLockKey);
                log.debug("Deleted the cache lock: [{}]", cacheLock);
            }
        }
    }

    private String buildCacheLockKey(@NonNull CacheLock cacheLock, @NonNull ProceedingJoinPoint joinPoint) {
        Assert.notNull(cacheLock, "Cache lock must not be null");
        Assert.notNull(joinPoint, "Proceeding join point must not be null");

        // Get the method
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        // Build the cache lock key
        StringBuilder cacheKeyBuilder = new StringBuilder(CACHE_LOCK_PREFOX);

        String delimiter = cacheLock.delimiter();

        if (StringUtils.isNotBlank(cacheLock.prefix())) {
            cacheKeyBuilder.append(cacheLock.prefix());
        }

        // Handle cache lock key building
        Annotation[][] parameterAnnotations = methodSignature.getMethod().getParameterAnnotations();

        for (int i = 0; i < parameterAnnotations.length; i++) {
            log.debug("Parameter annotation[{}] = {}", i, parameterAnnotations[i]);

            for (int j = 0; j < parameterAnnotations[i].length; j++) {
                Annotation annotation = parameterAnnotations[i][j];
                log.debug("Parameter annotation[{}][{}]: {}", i, j, annotation);
                if (annotation instanceof CacheParam) {
                    // Get current argument
                    Object arg = joinPoint.getArgs()[i];
                    log.debug("Cache param args: [{}]", arg);

                    // Append to the cache key
                    cacheKeyBuilder.append(delimiter).append(arg.toString());
                }
            }
        }

        if (cacheLock.traceRequest()) {
            // Append http request info
            cacheKeyBuilder.append(delimiter).append(ServletUtil.getClientIP(httpServletRequest));
        }

        return cacheKeyBuilder.toString();
    }
}
