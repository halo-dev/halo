package run.halo.app.infra.messaging;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import org.springframework.cache.CacheManager;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import run.halo.app.infra.properties.HaloProperties;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Listens to Redis Stream events and processes them for cache synchronization.
 */
@Slf4j
@Component
public class RedisStreamEventListener implements InitializingBean {

    private final StringRedisTemplate redisTemplate;
    private final HaloProperties properties;
    private final CacheManager cacheManager;

    public RedisStreamEventListener(StringRedisTemplate redisTemplate, HaloProperties properties, CacheManager cacheManager) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.cacheManager = cacheManager;
    }

    @Override
    public void afterPropertiesSet() {
        if (!properties.getDistributed().isEnabled()) {
            return;
        }
        // Create consumer group if not exists
        try {
            redisTemplate.opsForStream()
                    .createGroup(properties.getDistributed().getStreamKey(), properties.getDistributed().getConsumerGroup());
        } catch (Exception e) {
            // Group might already exist
        }
    }

    /**
     * Polls the Redis Stream for new events and acknowledges them after processing.
     */
    @Scheduled(fixedDelayString = "${halo.distributed.listener-interval:1000}")
    public void listen() {
        if (!properties.getDistributed().isEnabled()) {
            return;
        }
        List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream()
                .read(Consumer.from(properties.getDistributed().getConsumerGroup(), UUID.randomUUID().toString()),
                        org.springframework.data.redis.connection.stream.StreamReadOptions.empty()
                                .count(10)
                                .block(Duration.ofMillis(500)),
                        StreamOffset.create(properties.getDistributed().getStreamKey(), ReadOffset.lastConsumed()));
        if (records != null) {
            for (MapRecord<String, Object, Object> record : records) {
                Map<Object, Object> rawEvent = record.getValue();
                String entity = rawEvent.get("entity") != null ? rawEvent.get("entity").toString() : null;
                String id = rawEvent.get("id") != null ? rawEvent.get("id").toString() : null;
                // Evict caches related to this entity and id
                for (String cacheName : cacheManager.getCacheNames()) {
                    if (cacheName.equals(entity) || cacheName.startsWith(entity)) {
                        org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
                        if (cache != null) {
                            cache.evict(id);
                            log.info("Evicted cache '{}' for key '{}'", cacheName, id);
                        }
                    }
                }
                // Acknowledge message
                redisTemplate.opsForStream().acknowledge(properties.getDistributed().getStreamKey(),
                        properties.getDistributed().getConsumerGroup(), record.getId());
            }
        }
    }
}