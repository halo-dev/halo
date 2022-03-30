package run.halo.app.cache;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * redis cache store.
 *
 * @author luoxx
 */
@Slf4j
public class RedisCacheStore extends AbstractStringCacheStore {

    private static final String REDIS_PREFIX = "halo.redis.";

    private final StringRedisTemplate redisTemplate;

    public RedisCacheStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    @NonNull
    Optional<CacheWrapper<String>> getInternal(@NonNull String key) {
        Assert.hasText(key, "Cache key must not be blank");
        String value = redisTemplate.opsForValue().get(REDIS_PREFIX + key);
        CacheWrapper<String> cacheStore = new CacheWrapper<>();
        cacheStore.setData(value);
        return Optional.of(cacheStore);
    }

    @Override
    void putInternal(@NonNull String key, @NonNull CacheWrapper<String> cacheWrapper) {
        Assert.hasText(key, "Cache key must not be blank");
        Assert.notNull(cacheWrapper, "Cache wrapper must not be null");
        if (cacheWrapper.getExpireAt() != null) {
            long expire = cacheWrapper.getExpireAt().getTime() - System.currentTimeMillis();
            redisTemplate.opsForValue().set(
                REDIS_PREFIX + key, cacheWrapper.getData(), expire, TimeUnit.MILLISECONDS);
        } else {
            redisTemplate.opsForValue().set(REDIS_PREFIX + key, cacheWrapper.getData());
        }

        log.debug("Put [{}] cache : [{}]", key, cacheWrapper);
    }

    @Override
    Boolean putInternalIfAbsent(@NonNull String key, @NonNull CacheWrapper<String> cacheWrapper) {
        Assert.hasText(key, "Cache key must not be blank");
        Assert.notNull(cacheWrapper, "Cache wrapper must not be null");

        log.debug("Preparing to put key: [{}], value: [{}]", key, cacheWrapper);

        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            log.warn("Failed to put the cache, the key: [{}] has been present already", key);
            return false;
        }

        putInternal(key, cacheWrapper);
        log.debug("Put successfully");
        return true;

    }

    @Override
    public Optional<String> get(String key) {
        Assert.notNull(key, "Cache key must not be blank");

        return getInternal(key).map(CacheWrapper::getData);
    }

    @Override
    public void delete(@NonNull String key) {
        Assert.hasText(key, "Cache key must not be blank");

        if (Boolean.TRUE.equals(redisTemplate.hasKey(REDIS_PREFIX + key))) {
            redisTemplate.delete(REDIS_PREFIX + key);
            log.debug("Removed key: [{}]", key);
        }
    }

    @Override
    public LinkedHashMap<String, String> toMap() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        Set<String> keys = redisTemplate.keys(REDIS_PREFIX + "*");
        if (CollectionUtils.isEmpty(keys)) {
            return map;
        }
        keys.forEach(key -> map.put(key, redisTemplate.opsForValue().get(key)));
        return map;
    }

    @PreDestroy
    public void preDestroy() {
        //do nothing
    }

}
