package run.halo.app.cache;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * redis cache store.
 *
 * @author luoxx
 */
@Slf4j
public class RedisCacheStore extends AbstractStringCacheStore {

    @Autowired
    private RedisTemplate<String, CacheWrapper<String>> redisTemplate;

    private static final String REDIS_PREFIX = "halo.redis.";

    @Override
    @NonNull
    Optional<CacheWrapper<String>> getInternal(@NonNull String key) {
        Assert.hasText(REDIS_PREFIX + key, "Cache key must not be blank");

        return Optional.ofNullable(redisTemplate.opsForValue().get(REDIS_PREFIX + key));
    }

    @Override
    void putInternal(@NonNull String key, @NonNull CacheWrapper<String> cacheWrapper) {
        Assert.hasText(key, "Cache key must not be blank");
        Assert.notNull(cacheWrapper, "Cache wrapper must not be null");
        if (cacheWrapper.getExpireAt() != null) {
            long expire = cacheWrapper.getExpireAt().getTime() - System.currentTimeMillis();
            redisTemplate.opsForValue().set(
                REDIS_PREFIX + key, cacheWrapper, expire, TimeUnit.MILLISECONDS);
        } else {
            redisTemplate.opsForValue().set(REDIS_PREFIX + key, cacheWrapper);
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
        for (String key : keys) {
            CacheWrapper<String> cacheWrapper = redisTemplate.opsForValue().get(key);
            if (cacheWrapper != null) {
                map.put(key, cacheWrapper.getData());
            }
        }
        return map;
    }

    @PreDestroy
    public void preDestroy() {
        //do nothing
    }

}
