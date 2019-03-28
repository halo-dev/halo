package cc.ryanc.halo.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * In-memory cache store.
 *
 * @author johnniang
 */
@Slf4j
public class InMemoryCacheStore extends StringCacheStore {

    /**
     * Cache container.
     */
    private final static ConcurrentHashMap<String, CacheWrapper<String>> cacheContainer = new ConcurrentHashMap<>();

    /**
     * Lock.
     */
    private Lock lock = new ReentrantLock();

    @Override
    Optional<CacheWrapper<String>> getInternal(String key) {
        Assert.hasText(key, "Cache key must not be blank");

        return Optional.ofNullable(cacheContainer.get(key));
    }

    @Override
    void putInternal(String key, CacheWrapper<String> cacheWrapper) {
        Assert.hasText(key, "Cache key must not be blank");
        Assert.notNull(cacheWrapper, "Cache wrapper must not be null");

        // Put the cache wrapper
        CacheWrapper<String> putCacheWrapper = cacheContainer.put(key, cacheWrapper);

        log.debug("Put cache wrapper: [{}]", putCacheWrapper);
    }

    @Override
    Boolean putInternalIfAbsent(String key, CacheWrapper<String> cacheWrapper) {
        Assert.hasText(key, "Cache key must not be blank");
        Assert.notNull(cacheWrapper, "Cache wrapper must not be null");

        log.debug("Preparing to put key: [{}], value: [{}]", key, cacheWrapper);

        try {
            lock.lock();
            // Get the value before
            Optional<String> valueOptional = get(key);

            if (valueOptional.isPresent()) {
                log.warn("Failed to put the cache, because the key: [{}] has been present already", key);
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
    public void delete(String key) {
        Assert.hasText(key, "Cache key must not be blank");

        cacheContainer.remove(key);
    }

    /**
     * Cache store cleaner.
     *
     * @author johnniang
     * @date 03/28/19
     */
    private class CacheStoreCleaner extends TimerTask {

        @Override
        public void run() {

            cacheContainer.keySet().forEach(InMemoryCacheStore.this::get);
        }
    }
}
