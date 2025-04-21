package run.halo.app.infra.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.infra.properties.HaloProperties;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DistributedHealthIndicator}.
 *
 * @author halo-dev
 * @since 2.20.18
 */
@ExtendWith(MockitoExtension.class)
class DistributedHealthIndicatorTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    
    @Mock
    private ValueOperations<String, String> valueOperations;
    
    @Mock
    private HaloProperties haloProperties;
    
    @Mock
    private HaloProperties.DistributedProperties distributedProperties;
    
    private DistributedHealthIndicator healthIndicator;
    
    @BeforeEach
    void setUp() {
        when(haloProperties.getDistributed()).thenReturn(distributedProperties);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        healthIndicator = new DistributedHealthIndicator(redisTemplate, haloProperties);
    }
    
    @Test
    void shouldReturnUpWhenDistributedModeIsDisabled() {
        // Given
        when(distributedProperties.isEnabled()).thenReturn(false);
        
        // When
        Mono<Health> healthMono = healthIndicator.health();
        
        // Then
        StepVerifier.create(healthMono)
            .expectNextMatches(health -> {
                return health.getStatus() == Status.UP &&
                       "disabled".equals(health.getDetails().get("distributedMode"));
            })
            .verifyComplete();
        
        // Verify no Redis operations were performed
        verify(redisTemplate, never()).opsForValue();
    }
    
    @Test
    void shouldReturnUpWhenRedisIsHealthy() {
        // Given
        when(distributedProperties.isEnabled()).thenReturn(true);
        when(valueOperations.get(anyString())).thenReturn("ok");
        
        // When
        Mono<Health> healthMono = healthIndicator.health();
        
        // Then
        StepVerifier.create(healthMono)
            .expectNextMatches(health -> {
                return health.getStatus() == Status.UP &&
                       "enabled".equals(health.getDetails().get("distributedMode")) &&
                       "healthy".equals(health.getDetails().get("redisConnection"));
            })
            .verifyComplete();
        
        // Verify Redis operations were performed
        verify(valueOperations).set(eq("halo:health:check"), eq("ok"), any(Duration.class));
        verify(valueOperations).get("halo:health:check");
    }
    
    @Test
    void shouldReturnDownWhenRedisReturnsDifferentValue() {
        // Given
        when(distributedProperties.isEnabled()).thenReturn(true);
        when(valueOperations.get(anyString())).thenReturn("not-ok");
        
        // When
        Mono<Health> healthMono = healthIndicator.health();
        
        // Then
        StepVerifier.create(healthMono)
            .expectNextMatches(health -> {
                return health.getStatus() == Status.DOWN &&
                       "enabled".equals(health.getDetails().get("distributedMode")) &&
                       "unhealthy".equals(health.getDetails().get("redisConnection")) &&
                       health.getDetails().containsKey("error");
            })
            .verifyComplete();
    }
    
    @Test
    void shouldReturnDownWhenRedisThrowsException() {
        // Given
        when(distributedProperties.isEnabled()).thenReturn(true);
        when(valueOperations.set(anyString(), anyString(), any(Duration.class)))
            .thenThrow(new RuntimeException("Redis connection refused"));
        
        // When
        Mono<Health> healthMono = healthIndicator.health();
        
        // Then
        StepVerifier.create(healthMono)
            .expectNextMatches(health -> {
                return health.getStatus() == Status.DOWN &&
                       "enabled".equals(health.getDetails().get("distributedMode")) &&
                       "unhealthy".equals(health.getDetails().get("redisConnection")) &&
                       health.getDetails().containsKey("error");
            })
            .verifyComplete();
    }
}
