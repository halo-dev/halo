package run.halo.app.infra.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import run.halo.app.infra.properties.HaloProperties;

import java.util.Map;

/**
 * Publishes domain events to a Redis Stream for cross-instance notifications.
 */
@Slf4j
@Component
public class RedisStreamEventPublisher {

    private final StringRedisTemplate redisTemplate;
    private final HaloProperties properties;

    public RedisStreamEventPublisher(StringRedisTemplate redisTemplate, HaloProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    /**
     * Publish an event to the configured Redis Stream.
     * @param event key-value pairs representing the event payload
     */
    public void publish(Map<String, String> event) {
        if (!properties.getDistributed().isEnabled()) {
            return;
        }
        try {
            redisTemplate.opsForStream()
                    .add(properties.getDistributed().getStreamKey(), event);
            log.debug("Published event to stream: {}", event);
        } catch (Exception e) {
            log.error("Failed to publish event to Redis Stream: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Publish a distributed event to the Redis Stream.
     * @param event The event to publish
     */
    public void publish(DistributedEvent event) {
        if (!properties.getDistributed().isEnabled()) {
            return;
        }
        publish(event.toStreamRecord());
    }
}