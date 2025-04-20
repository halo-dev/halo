package run.halo.app.infra.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import run.halo.app.infra.properties.HaloProperties;

/**
 * Configuration for cache management. Uses RedisCacheManager when distributed mode is enabled,
 * otherwise falls back to an in-memory concurrent map cache.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private final HaloProperties properties;

    public CacheConfig(HaloProperties properties) {
        this.properties = properties;
    }

    /**
     * Redis-based cache manager, active when distributed deployment is enabled.
     */
    @Bean
    @ConditionalOnProperty(prefix = "halo.distributed", name = "enabled", havingValue = "true")
    public CacheManager redisCacheManager(RedisConnectionFactory factory) {
        return RedisCacheManager.create(factory);
    }

    /**
     * In-memory cache manager, active when distributed deployment is disabled or not configured.
     */
    @Bean
    @ConditionalOnProperty(prefix = "halo.distributed", name = "enabled", havingValue = "false", matchIfMissing = true)
    public CacheManager concurrentMapCacheManager() {
        return new ConcurrentMapCacheManager();
    }
}