package cc.ryanc.halo.cache;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Abstract cache store.
 *
 * @author johnniang
 * @date 3/28/19
 */
@Slf4j
public abstract class AbstractCacheStore<K, V> implements CacheStore<K, V> {

    /**
     * Get cache wrapper by key.
     *
     * @param key key must not be null
     * @return an optional cache wrapper
     */
    @NonNull
    abstract Optional<CacheWrapper<V>> getInternal(@NonNull K key);

    /**
     * Puts the cache wrapper.
     *
     * @param key          key must not be null
     * @param cacheWrapper cache wrapper must not be null
     */
    abstract void putInternal(@NonNull K key, @NonNull CacheWrapper<V> cacheWrapper);

    @Override
    public Optional<V> get(K key) {
        Assert.notNull(key, "Cache key must not be blank");

        return getInternal(key).map(cacheWrapper -> {
            log.debug("Cache wrapper: [{}]", cacheWrapper);

            // Check expiration
            if (cacheWrapper.getExpireAt() != null && cacheWrapper.getExpireAt().before(cc.ryanc.halo.utils.DateUtils.now())) {
                // Expired then delete it
                log.warn("Cache key: [{}] has been expired", key);

                // Delete the key
                delete(key);

                // Return null
                return null;
            }

            return cacheWrapper.getData();
        });
    }

    @Override
    public void put(K key, V value, long timeout, TimeUnit timeUnit) {
        Assert.notNull(key, "Cache key must not be blank");
        Assert.notNull(value, "Cache value must not be null");
        Assert.isTrue(timeout > 0, "Cache expiration timeout must not be less than 1");
        Assert.notNull(timeUnit, "Time unit must not be null");

        // Handle expiration
        Date now = cc.ryanc.halo.utils.DateUtils.now();

        long millis = timeUnit.toMillis(timeout);
        if (millis <= 0) {
            millis = 1L;
        }

        Date expireAt = DateUtils.addMilliseconds(now, Long.valueOf(millis).intValue());

        // Build cache wrapper
        CacheWrapper<V> cacheWrapper = new CacheWrapper<>();
        cacheWrapper.setCreateAt(now);
        cacheWrapper.setExpireAt(expireAt);
        cacheWrapper.setData(value);

        putInternal(key, cacheWrapper);
    }

    @Override
    public void put(K key, V value) {
        Assert.notNull(key, "Cache key must not be blank");
        Assert.notNull(value, "Cache value must not be null");

        // Get current time
        Date now = cc.ryanc.halo.utils.DateUtils.now();

        // Build cache wrapper
        CacheWrapper<V> cacheWrapper = new CacheWrapper<>();
        cacheWrapper.setCreateAt(now);
        cacheWrapper.setExpireAt(null);
        cacheWrapper.setData(value);

        putInternal(key, cacheWrapper);

    }
}
