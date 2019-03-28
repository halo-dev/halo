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

        // Put the cache wrapper
        CacheWrapper<String> putCacheWrapper = cacheContainer.putIfAbsent(key, cacheWrapper);

        return cacheWrapper.equals(putCacheWrapper);
    }

    @Override
    public void delete(String key) {
        Assert.hasText(key, "Cache key must not be blank");

        cacheContainer.remove(key);
    }
}
