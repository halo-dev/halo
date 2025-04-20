package run.halo.app.infra.messaging;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import run.halo.app.infra.properties.HaloProperties;

import java.util.Map;

/**
 * Publishes domain events to a Redis Stream for cross-instance notifications.
 */
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
        redisTemplate.opsForStream()
                .add(properties.getDistributed().getStreamKey(), event);
    }
}