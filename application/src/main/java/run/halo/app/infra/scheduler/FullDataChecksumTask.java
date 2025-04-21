package run.halo.app.infra.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.properties.HaloProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * A scheduled task that periodically verifies data integrity across distributed Halo instances.
 * It generates checksums for key entities and stores them in Redis, then instances can verify
 * their local caches against these checksums to ensure consistency.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FullDataChecksumTask {

    private final ReactiveExtensionClient client;
    private final CacheManager cacheManager;
    private final StringRedisTemplate redisTemplate;
    private final HaloProperties properties;

    private static final String CHECKSUM_KEY_PREFIX = "halo:checksum:";
    private static final int BATCH_SIZE = 100;

    /**
     * Scheduled task to run the full data checksum verification.
     * Uses ShedLock to ensure only one instance runs this at a time in a distributed environment.
     */
    @Scheduled(cron = "${halo.distributed.full-checksum-cron:0 0 * * * ?}") // Default: Every hour
    @SchedulerLock(name = "fullDataChecksum", lockAtLeastFor = "5m", lockAtMostFor = "30m")
    public void runFullChecksum() {
    if (!properties.getDistributed().isEnabled()) {
    return;
    }
    
    log.info("Starting full data checksum verification task");
    
    // Generate checksums for posts
    generatePostChecksums()
    .doOnSuccess(result -> log.info("Post checksums completed"))
    .subscribe();
    
    // Add other entity checksum generation here:
    // generateCommentChecksums().subscribe();
    // generateTagChecksums().subscribe();
    // etc.
    }
    
    /**
     * Calculates checksums for all posts and stores them in Redis.
     * 
     * @return A Mono that completes when the operation is done
     */
    private Mono<Void> generatePostChecksums() {
        String checksumKey = CHECKSUM_KEY_PREFIX + "posts";
        
        // Clear existing checksums
        return Mono.fromCallable(() -> redisTemplate.delete(checksumKey))
            .then(
                // Process posts in batches
                client.listAll(Post.class, new ListOptions(), Sort.by("metadata.creationTimestamp"))
                    .buffer(BATCH_SIZE)
                    .flatMap(posts -> {
                        Map<String, String> checksums = new HashMap<>();
                        for (Post post : posts) {
                            String name = post.getMetadata().getName();
                            String resourceVersion = post.getMetadata().getVersion().toString();
                            checksums.put(name, resourceVersion);
                        }
                        
                        // Store checksums in Redis
                        Boolean result = redisTemplate.opsForHash().putAll(checksumKey, checksums);
                        return Mono.just(result);
                    })
                    .then()
                    // Set expiration to avoid stale data
                    .then(Mono.fromCallable(() -> 
                        redisTemplate.expire(checksumKey, Duration.ofDays(1))
                    ))
                    .then()
            );
    }
    
    /**
     * Verifies local post cache against the checksums in Redis and refreshes if needed.
     * This is called periodically by each instance.
     */
    public void verifyAndRefreshPostCache() {
        if (!properties.getDistributed().isEnabled()) {
            return;
        }
        
        String checksumKey = CHECKSUM_KEY_PREFIX + "posts";
        
        // Get all post checksums from Redis
        Map<Object, Object> checksums = redisTemplate.opsForHash().entries(checksumKey);
        if (checksums == null || checksums.isEmpty()) {
            log.debug("No checksums found, skipping cache verification");
            return;
        }
        
        log.info("Verifying {} post checksums against cache", checksums.size());
        
        // Primary post cache
        var postCache = cacheManager.getCache("posts");
        if (postCache == null) {
            log.warn("Post cache not found, skipping verification");
            return;
        }
        
        // Check each post and refresh cache if needed
        int refreshedCount = 0;
        for (Map.Entry<Object, Object> entry : checksums.entrySet()) {
            String postName = entry.getKey().toString();
            String resourceVersion = entry.getValue().toString();
            
            // Get from cache if exists
            Post cachedPost = postCache.get(postName, Post.class);
            if (cachedPost == null || 
                !resourceVersion.equals(cachedPost.getMetadata().getVersion().toString())) {
                
                // Cache miss or version mismatch, refresh from database
                refreshedCount++;
                client.fetch(Post.class, postName)
                    .doOnNext(post -> {
                        postCache.put(postName, post);
                        log.debug("Refreshed cache for post {}", postName);
                    })
                    .subscribe();
            }
        }
        
        if (refreshedCount > 0) {
            log.info("Refreshed {} posts in cache based on checksum verification", refreshedCount);
        } else {
            log.debug("All post cache entries are up to date");
        }
    }
}