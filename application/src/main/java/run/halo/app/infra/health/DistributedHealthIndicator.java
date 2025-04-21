package run.halo.app.infra.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.infra.properties.HaloProperties;

import java.time.Duration;

/**
 * Health indicator that checks the Redis connection and communication
 * specifically for distributed deployment mode.
 *
 * @author halo-dev
 * @since 2.20.18
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedHealthIndicator implements ReactiveHealthIndicator {
    
    private final StringRedisTemplate redisTemplate;
    private final HaloProperties properties;
    
    private static final String HEALTH_CHECK_KEY = "halo:health:check";
    private static final String HEALTH_CHECK_VALUE = "ok";
    private static final Duration HEALTH_CHECK_TIMEOUT = Duration.ofSeconds(5);
    
    @Override
    public Mono<Health> health() {
        // Skip the check if distributed mode is not enabled
        if (!properties.getDistributed().isEnabled()) {
            return Mono.just(Health.up()
                .withDetail("distributedMode", "disabled")
                .build());
        }
        
        return Mono.fromCallable(() -> {
                try {
                    // Try to set a value in Redis
                    redisTemplate.opsForValue().set(
                        HEALTH_CHECK_KEY, 
                        HEALTH_CHECK_VALUE, 
                        HEALTH_CHECK_TIMEOUT
                    );
                    
                    // Try to read it back
                    String value = redisTemplate.opsForValue().get(HEALTH_CHECK_KEY);
                    
                    // Check if the value matches
                    return HEALTH_CHECK_VALUE.equals(value);
                } catch (Exception e) {
                    log.error("Redis health check failed", e);
                    return false;
                }
            })
            .map(isHealthy -> isHealthy 
                ? Health.up()
                    .withDetail("distributedMode", "enabled")
                    .withDetail("redisConnection", "healthy")
                    .build()
                : Health.down()
                    .withDetail("distributedMode", "enabled")
                    .withDetail("redisConnection", "unhealthy")
                    .withDetail("error", "Redis communication failure")
                    .build()
            )
            .onErrorResume(e -> {
                log.error("Error during distributed health check", e);
                return Mono.just(Health.down(e)
                    .withDetail("distributedMode", "enabled")
                    .withDetail("error", "Health check failed: " + e.getMessage())
                    .build());
            });
    }
}
