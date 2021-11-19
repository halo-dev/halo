package run.halo.app.cache;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * In-memory cache store.
 *
 * @author johnniang
 */
@Slf4j
public class InMemoryCacheStore extends AbstractStringCacheStore {

    /**
     * Cleaner schedule period. (ms)
     */
    private static final long PERIOD = 60 * 1000;

    /**
     * Cache container.
     */
    private static final ConcurrentHashMap<String, CacheWrapper<String>> CACHE_CONTAINER =
        new ConcurrentHashMap<>();

    private final Timer timer;

    /**
     * Lock.
     */
    private final Lock lock = new ReentrantLock();

    public InMemoryCacheStore() {
        // Run a cache store cleaner
        timer = new Timer();
        timer.scheduleAtFixedRate(new CacheExpiryCleaner(), 0, PERIOD);
    }

    @Override
    @NonNull
    Optional<CacheWrapper<String>> getInternal(@NonNull String key) {
        Assert.hasText(key, "Cache key must not be blank");

        return Optional.ofNullable(CACHE_CONTAINER.get(key));
    }

    @Override
    void putInternal(@NonNull String key, @NonNull CacheWrapper<String> cacheWrapper) {
        Assert.hasText(key, "Cache key must not be blank");
        Assert.notNull(cacheWrapper, "Cache wrapper must not be null");

        // Put the cache wrapper
        CacheWrapper<String> putCacheWrapper = CACHE_CONTAINER.put(key, cacheWrapper);
        log.debug("Put [{}] cache result: [{}], original cache wrapper: [{}]", key, putCacheWrapper,
            cacheWrapper);
    }

    @Override
    Boolean putInternalIfAbsent(@NonNull String key, @NonNull CacheWrapper<String> cacheWrapper) {
        Assert.hasText(key, "Cache key must not be blank");
        Assert.notNull(cacheWrapper, "Cache wrapper must not be null");

        log.debug("Preparing to put key: [{}], value: [{}]", key, cacheWrapper);

        lock.lock();
        try {
            // Get the value before
            Optional<String> valueOptional = get(key);

            if (valueOptional.isPresent()) {
                log.warn("Failed to put the cache, because the key: [{}] has been present already",
                    key);
                return false;
            }

            // Put the cache wrapper
            putInternal(key, cacheWrapper);
            log.debug("Put successfully");
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void delete(@NonNull String key) {
        Assert.hasText(key, "Cache key must not be blank");

        CACHE_CONTAINER.remove(key);
        log.debug("Removed key: [{}]", key);
    }

    @Override
    public LinkedHashMap<String, String> toMap() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        CACHE_CONTAINER.forEach((key, value) -> map.put(key, value.getData()));
        return map;
    }

    @PreDestroy
    public void preDestroy() {
        log.debug("Cancelling all timer tasks");
        timer.cancel();
        clear();
    }

    public void clear() {
        CACHE_CONTAINER.clear();
    }

    /**
     * Cache cleaner.
     *
     * @author johnniang
     * @date 03/28/19
     */
    private class CacheExpiryCleaner extends TimerTask {

        @Override
        public void run() {
            CACHE_CONTAINER.keySet().forEach(key -> {
                if (!InMemoryCacheStore.this.get(key).isPresent()) {
                    log.debug("Deleted the cache: [{}] for expiration", key);
                }
            });
        }
    }
}
