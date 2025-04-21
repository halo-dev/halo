package run.halo.app.infra.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.scheduler.FullDataChecksumTask;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens to Redis Stream events and processes them for cache synchronization.
 */
@Slf4j
@Component
public class RedisStreamEventListener implements InitializingBean {

    private final StringRedisTemplate redisTemplate;
    private final HaloProperties properties;
    private final CacheManager cacheManager;
    private final FullDataChecksumTask checksumTask;
    
    // 用于消息幂等性检查的集合，存储最近处理过的消息ID
    private final Set<String> processedMessageIds = ConcurrentHashMap.newKeySet();
    
    // 消息ID缓存过期时间（毫秒）
    private static final long MESSAGE_ID_EXPIRATION = 3600000; // 1小时
    
    // 记录每个消息ID的处理时间，用于过期检查
    private final Map<String, Long> messageProcessTimes = new ConcurrentHashMap<>();

    public RedisStreamEventListener(StringRedisTemplate redisTemplate, 
                                   HaloProperties properties, 
                                   CacheManager cacheManager,
                                   FullDataChecksumTask checksumTask) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.cacheManager = cacheManager;
        this.checksumTask = checksumTask;
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
            log.info("Created or connected to consumer group: {}", properties.getDistributed().getConsumerGroup());
        } catch (Exception e) {
            // Group might already exist
            log.debug("Consumer group may already exist: {}", e.getMessage());
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
        
        try {
            List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream()
                    .read(Consumer.from(properties.getDistributed().getConsumerGroup(), UUID.randomUUID().toString()),
                            org.springframework.data.redis.connection.stream.StreamReadOptions.empty()
                                    .count(10)
                                    .block(Duration.ofMillis(500)),
                            StreamOffset.create(properties.getDistributed().getStreamKey(), ReadOffset.lastConsumed()));
            
            if (records != null) {
                for (MapRecord<String, Object, Object> record : records) {
                    String messageId = record.getId().getValue();
                    
                    // 幂等性检查 - 如果消息已处理过，则跳过
                    if (processedMessageIds.contains(messageId)) {
                        log.debug("Skipping already processed message: {}", messageId);
                        // 确认消息
                        acknowledgeMessage(messageId);
                        continue;
                    }
                    
                    try {
                        Map<Object, Object> rawEvent = record.getValue();
                        processEvent(rawEvent, messageId);
                        
                        // 记录已处理的消息ID和处理时间
                        processedMessageIds.add(messageId);
                        messageProcessTimes.put(messageId, System.currentTimeMillis());
                        
                        // 确认消息
                        acknowledgeMessage(messageId);
                    } catch (Exception e) {
                        log.error("Error processing message {}: {}", messageId, e.getMessage(), e);
                        // 对于处理失败的消息，我们不确认，让它在下一次被再次处理
                        // 这里可以添加重试计数和死信队列逻辑
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in Redis Stream listener", e);
        }
    }
    
    /**
     * 处理事件逻辑
     */
    private void processEvent(Map<Object, Object> rawEvent, String messageId) {
        String type = rawEvent.get("type") != null ? rawEvent.get("type").toString() : null;
        String entity = rawEvent.get("entity") != null ? rawEvent.get("entity").toString() : null;
        String id = rawEvent.get("id") != null ? rawEvent.get("id").toString() : null;
        String operation = rawEvent.get("operation") != null ? rawEvent.get("operation").toString() : null;
        
        if (entity == null || id == null) {
            log.warn("Received invalid event without entity or id: {}", messageId);
            return;
        }
        
        log.debug("Processing event: type={}, entity={}, id={}, operation={}", 
                 type, entity, id, operation);
        
        // 根据事件类型和实体类型选择性地失效缓存
        invalidateCache(entity, id, operation);
    }
    
    /**
     * 根据事件类型选择性地失效缓存
     */
    private void invalidateCache(String entity, String id, String operation) {
        // 主缓存
        String mainCacheName = entity.toLowerCase() + "s"; // 例如：posts, comments
        evictFromCache(mainCacheName, id);
        
        // 相关缓存
        if ("Post".equals(entity)) {
            // 文章相关的其他缓存
            evictFromCache("postLists", "*");
            evictFromCache("categories", "*");
            evictFromCache("tags", "*");
        } else if ("Comment".equals(entity)) {
            // 评论相关的其他缓存
            String postId = extractPostIdFromComment(id);
            if (postId != null) {
                evictFromCache("postComments", postId);
            }
        }
        // ... 处理其他实体类型 ...
    }
    
    private void evictFromCache(String cacheName, String key) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            if ("*".equals(key)) {
                cache.clear();
                log.debug("Cleared entire cache: {}", cacheName);
            } else {
                cache.evict(key);
                log.debug("Evicted from cache: {}:{}", cacheName, key);
            }
        }
    }
    
    private String extractPostIdFromComment(String commentId) {
        // 实现从评论ID提取文章ID的逻辑
        // 这取决于你的评论ID格式或需要查询数据库
        return null;
    }
    
    /**
     * 确认消息
     */
    private void acknowledgeMessage(String messageId) {
        try {
            redisTemplate.opsForStream().acknowledge(
                properties.getDistributed().getStreamKey(),
                properties.getDistributed().getConsumerGroup(), 
                messageId);
            log.debug("Acknowledged message: {}", messageId);
        } catch (Exception e) {
            log.error("Failed to acknowledge message: {}", messageId, e);
        }
    }
    
    /**
     * 清理过期的消息ID记录
     */
    @Scheduled(fixedDelay = 60000) // 每分钟执行一次
    public void cleanupProcessedMessageIds() {
        if (!properties.getDistributed().isEnabled()) {
            return;
        }
        
        long now = System.currentTimeMillis();
        int removedCount = 0;
        
        for (Map.Entry<String, Long> entry : messageProcessTimes.entrySet()) {
            boolean expired = (now - entry.getValue()) > MESSAGE_ID_EXPIRATION;
            if (expired) {
                processedMessageIds.remove(entry.getKey());
                messageProcessTimes.remove(entry.getKey());
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            log.debug("Cleaned up {} expired message IDs", removedCount);
        }
    }
    
    /**
     * 定期触发全量校验
     */
    @Scheduled(cron = "0 15 * * * ?") // 每小时15分执行
    public void triggerFullVerification() {
        if (!properties.getDistributed().isEnabled()) {
            return;
        }
        checksumTask.verifyAndRefreshPostCache();
    }
}