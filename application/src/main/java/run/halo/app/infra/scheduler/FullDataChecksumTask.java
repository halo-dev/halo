package run.halo.app.infra.scheduler;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.properties.HaloProperties;

import run.halo.app.extension.Extension;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled task that performs a full data checksum and cache synchronization across instances.
 */
@Slf4j
@Component
public class FullDataChecksumTask {

    private final HaloProperties properties;
    private final ReactiveExtensionClient client;
    private final StringRedisTemplate redisTemplate;
    private final CacheManager cacheManager;
    
    private static final String CHECKSUM_KEY_PREFIX = "halo:checksum:";
    private static final int BATCH_SIZE = 100;

    public FullDataChecksumTask(HaloProperties properties, 
                               ReactiveExtensionClient client,
                               StringRedisTemplate redisTemplate,
                               CacheManager cacheManager) {
        this.properties = properties;
        this.client = client;
        this.redisTemplate = redisTemplate;
        this.cacheManager = cacheManager;
    }

    /**
     * Runs hourly to perform full data checksum and cache recovery.
     */
    @Scheduled(cron = "0 0 * * * *")
    @SchedulerLock(name = "FullDataChecksumTask", lockAtMostFor = "PT59M", lockAtLeastFor = "PT1M")
    public void run() {
        if (!properties.getDistributed().isEnabled()) {
            return;
        }
        log.info("Executing full data checksum task for distributed cache synchronization.");
        
        // Calculate checksums for critical entities
        calculatePostChecksums()
            .then(calculateSinglePageChecksums())
            .then(calculateCommentChecksums())
            .then(calculateSnapshotChecksums())
            .then(syncAllCaches())
            .doOnError(e -> log.error("Error during full data checksum calculation", e))
            .subscribe();
    }
    
    /**
     * Calculate checksums for Post entities and store in Redis
     */
    private Mono<Void> calculatePostChecksums() {
        return calculateEntityChecksums(Post.class, "post");
    }
    
    /**
     * Calculate checksums for SinglePage entities and store in Redis
     */
    private Mono<Void> calculateSinglePageChecksums() {
        return calculateEntityChecksums(SinglePage.class, "singlepage");
    }
    
    /**
     * Calculate checksums for Comment entities and store in Redis
     */
    private Mono<Void> calculateCommentChecksums() {
        return calculateEntityChecksums(Comment.class, "comment");
    }
    
    /**
     * Calculate checksums for Snapshot entities and store in Redis
     */
    private Mono<Void> calculateSnapshotChecksums() {
        return calculateEntityChecksums(Snapshot.class, "snapshot");
    }
    
    /**
     * Generic method to calculate checksums for any entity type
     *
     * @param entityClass The class of entity to calculate checksums for
     * @param entityType The string identifier for this entity type
     * @return A Mono completing when checksums are calculated and stored
     */
    private <T extends Extension> Mono<Void> calculateEntityChecksums(Class<T> entityClass, String entityType) {
        log.info("Calculating checksums for {}", entityType);
        String checksumKey = CHECKSUM_KEY_PREFIX + entityType;
        
        // Clear previous checksums
        redisTemplate.delete(checksumKey);
        
        Map<String, String> checksums = new HashMap<>();
        
        return client.listAll(entityClass, new ListOptions(), null)
            .doOnNext(entity -> {
                String id = entity.getMetadata().getName();
                Long version = entity.getMetadata().getVersion();
                checksums.put(id, String.valueOf(version));
            })
            .collectList()
            .doOnSuccess(list -> {
                if (!checksums.isEmpty()) {
                    redisTemplate.opsForHash().putAll(checksumKey, checksums);
                    // Set expiry to ensure old data is eventually cleaned up
                    redisTemplate.expire(checksumKey, 24, TimeUnit.HOURS);
                    log.info("Stored {} checksums for {} entities", checksums.size(), entityType);
                }
            })
            .then();
    }
    
    /**
     * Compare local cache with Redis checksums and sync any differences
     */
    private Mono<Void> syncAllCaches() {
        log.info("Syncing local caches with distributed checksums");
        // Check each cache namespace against checksums
        return syncEntityCache("post")
            .then(syncEntityCache("singlepage"))
            .then(syncEntityCache("comment"))
            .then(syncEntityCache("snapshot"));
    }
    
    /**
     * Sync local caches for a specific entity type
     */
    private Mono<Void> syncEntityCache(String entityType) {
        String checksumKey = CHECKSUM_KEY_PREFIX + entityType;
        
        // Skip if Redis doesn't have checksums for this entity
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(checksumKey))) {
            return Mono.empty();
        }
        
        // Get all checksums from Redis
        Map<Object, Object> checksums = redisTemplate.opsForHash().entries(checksumKey);
        log.info("Processing {} checksums for {}", checksums.size(), entityType);
        
        // Get corresponding cache
        org.springframework.cache.Cache cache = cacheManager.getCache(entityType);
        if (cache == null) {
            log.warn("No cache found for entity type: {}", entityType);
            return Mono.empty();
        }
        
        // Evict cache entries based on checksums
        for (Map.Entry<Object, Object> entry : checksums.entrySet()) {
            String id = entry.getKey().toString();
            cache.evict(id);
        }
        
        log.info("Evicted cache entries for {} {}", checksums.size(), entityType);
        return Mono.empty();
    }
}