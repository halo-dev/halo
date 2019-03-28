package cc.ryanc.halo.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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

    @Override
    Optional<CacheWrapper<String>> getInternal(String key) {
        Assert.hasText(key, "Cache key must not be blank");

        return Optional.ofNullable(cacheContainer.get(key));
    }

    @Override
    synchronized void putInternal(String key, CacheWrapper<String> cacheWrapper) {
        Assert.hasText(key, "Cache key must not be blank");
        Assert.notNull(cacheWrapper, "Cache wrapper must not be null");

        // Put the cache wrapper
        CacheWrapper<String> putCacheWrapper = cacheContainer.put(key, cacheWrapper);

        log.debug("Put cache wrapper: [{}]", putCacheWrapper);
    }

    @Override
    synchronized Boolean putInternalIfAbsent(String key, CacheWrapper<String> cacheWrapper) {
        Assert.hasText(key, "Cache key must not be blank");
        Assert.notNull(cacheWrapper, "Cache wrapper must not be null");

        log.debug("Preparing to put key: [{}], value: [{}]", key, cacheWrapper);

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
    }

    @Override
    public synchronized void delete(String key) {
        Assert.hasText(key, "Cache key must not be blank");

        cacheContainer.remove(key);
    }
}
