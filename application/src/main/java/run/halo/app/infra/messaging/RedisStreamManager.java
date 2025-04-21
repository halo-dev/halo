package run.halo.app.infra.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import run.halo.app.infra.properties.HaloProperties;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import java.time.Duration;
import java.util.Map;

/**
 * Manages Redis Stream configuration, maintenance and health checks.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamManager {

    private final StringRedisTemplate redisTemplate;
    private final RedisConnectionFactory connectionFactory;
    private final HaloProperties properties;
    
    private static final int MAX_STREAM_LENGTH = 10000;
    
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (!properties.getDistributed().isEnabled()) {
            return;
        }
        
        String streamKey = properties.getDistributed().getStreamKey();
        String consumerGroup = properties.getDistributed().getConsumerGroup();
        
        try {
            // 确保Stream存在
            StreamInfo.XInfoStream streamInfo = redisTemplate.opsForStream()
                .info(streamKey);
            log.info("Redis Stream exists: {}, length: {}", 
                    streamKey, streamInfo.streamLength());
        } catch (Exception e) {
            // Stream不存在，创建一个空Stream
            log.info("Creating Redis Stream: {}", streamKey);
            redisTemplate.opsForStream().createGroup(
                streamKey, 
                ReadOffset.from("0-0"), 
                consumerGroup
            );
        }
        
        try {
            // 查看消费组信息
            StreamInfo.XInfoGroups groups = redisTemplate.opsForStream()
                .groups(streamKey);
            boolean groupExists = groups.stream()
                .anyMatch(group -> consumerGroup.equals(group.groupName()));
                
            if (!groupExists) {
                // 创建消费组
                log.info("Creating consumer group: {}", consumerGroup);
                redisTemplate.opsForStream().createGroup(
                    streamKey, 
                    ReadOffset.latest(), 
                    consumerGroup
                );
            } else {
                log.info("Consumer group exists: {}", consumerGroup);
            }
        } catch (Exception e) {
            log.error("Error checking/creating consumer group", e);
        }
    }
    
    /**
     * 修剪Stream，确保不超过最大长度
     */
    public void trimStream() {
        if (!properties.getDistributed().isEnabled()) {
            return;
        }
        
        String streamKey = properties.getDistributed().getStreamKey();
        
        try {
            // 获取Stream长度
            StreamInfo.XInfoStream streamInfo = redisTemplate.opsForStream()
                .info(streamKey);
            long streamLength = streamInfo.streamLength();
            
            // 如果长度超过阈值，修剪Stream
            if (streamLength > MAX_STREAM_LENGTH) {
                log.info("Trimming stream {} to {} messages (current: {})", 
                        streamKey, MAX_STREAM_LENGTH, streamLength);
                redisTemplate.opsForStream().trim(streamKey, MAX_STREAM_LENGTH);
            }
        } catch (Exception e) {
            log.error("Error trimming stream", e);
        }
    }
    
    /**
     * 清理消费组中的处理过的消息
     */
    public void cleanupProcessedMessages() {
        if (!properties.getDistributed().isEnabled()) {
            return;
        }
        
        String streamKey = properties.getDistributed().getStreamKey();
        String consumerGroup = properties.getDistributed().getConsumerGroup();
        
        try {
            // 获取消费组中的待处理消息
            PendingMessages pendingMessages = redisTemplate.opsForStream()
                .pending(streamKey, consumerGroup, Range.unbounded(), 1);
                
            if (pendingMessages.getCount() > 0) {
                log.info("Consumer group has {} pending messages", 
                        pendingMessages.getCount());
                
                // 如果有大量未处理的消息，可以考虑重新分配或清理
                if (pendingMessages.getCount() > 1000) {
                    log.warn("Large number of pending messages ({}), consider manual intervention", 
                            pendingMessages.getCount());
                }
            }
        } catch (Exception e) {
            log.error("Error checking pending messages", e);
        }
    }
    
    /**
     * 定期维护Redis Stream
     */
    @Scheduled(cron = "0 */30 * * * ?") // 每30分钟执行一次
    public void scheduledMaintenance() {
        if (!properties.getDistributed().isEnabled()) {
            return;
        }
        log.info("Performing scheduled Redis Stream maintenance");
        trimStream();
        cleanupProcessedMessages();
    }
}