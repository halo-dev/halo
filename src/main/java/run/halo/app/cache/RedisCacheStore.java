package run.halo.app.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.utils.JsonUtils;

import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Redis cache store.
 *
 * @author chaos
 */
@Slf4j
public class RedisCacheStore extends AbstractStringCacheStore {
    /**
     * Cache container.
     */
    private final static ConcurrentHashMap<String, CacheWrapper<String>> CACHE_CONTAINER = new ConcurrentHashMap<>();

    private volatile static JedisCluster REDIS;

    /**
     * Lock.
     */
    private final Lock lock = new ReentrantLock();

    public RedisCacheStore(HaloProperties haloProperties) {
        this.haloProperties = haloProperties;
        initRedis();
    }

    private void initRedis() {
        JedisPoolConfig cfg = new JedisPoolConfig();
        cfg.setMaxIdle(2);
        cfg.setMaxTotal(30);
        cfg.setMaxWaitMillis(5000);
        Set<HostAndPort> nodes = new HashSet<>();
        for (String hostPort : this.haloProperties.getCacheRedisNodes()) {
            String[] temp = hostPort.split(":");
            if (temp.length > 0) {
                String host = temp[0];
                int port = 6379;
                if (temp.length > 1) {
                    try {
                        port = Integer.parseInt(temp[1]);
                    } catch (Exception ex) {

                    }
                }
                nodes.add(new HostAndPort(host, port));
            }
        }
        if (nodes.isEmpty()) {
            nodes.add(new HostAndPort("127.0.0.1", 6379));
        }
        REDIS = new JedisCluster(nodes, 5, 20, 3, this.haloProperties.getCacheRedisPassword(), cfg);
        log.info("Initialized cache redis cluster: {}", REDIS.getClusterNodes());
    }

    @NotNull
    @Override
    Optional<CacheWrapper<String>> getInternal(@NotNull String key) {
        Assert.hasText(key, "Cache key must not be blank");
        String v = REDIS.get(key);
        return StringUtils.isEmpty(v) ? Optional.empty() : jsonToCacheWrapper(v);
    }

    @Override
    void putInternal(@NotNull String key, @NotNull CacheWrapper<String> cacheWrapper) {
        putInternalIfAbsent(key, cacheWrapper);
        try {
            REDIS.set(key, JsonUtils.objectToJson(cacheWrapper));
            Date ttl = cacheWrapper.getExpireAt();
            if (ttl != null) {
                REDIS.pexpireAt(key, ttl.getTime());
            }
        } catch (Exception e) {
            log.warn("Put cache fail json2object key: [{}] value:[{}]", key, cacheWrapper);
        }
    }

    @Override
    Boolean putInternalIfAbsent(@NotNull String key, @NotNull CacheWrapper<String> cacheWrapper) {
        Assert.hasText(key, "Cache key must not be blank");
        Assert.notNull(cacheWrapper, "Cache wrapper must not be null");
        try {
            if (REDIS.setnx(key, JsonUtils.objectToJson(cacheWrapper)) <= 0) {
                log.warn("Failed to put the cache, because the key: [{}] has been present already", key);
                return false;
            }
            Date ttl = cacheWrapper.getExpireAt();
            if (ttl != null) {
                REDIS.pexpireAt(key, ttl.getTime());
            }
            return true;
        } catch (JsonProcessingException e) {
            log.warn("Put cache fail json2object key: [{}] value:[{}]", key, cacheWrapper);
        }
        log.debug("Cache key: [{}], original cache wrapper: [{}]", key, cacheWrapper);
        return false;
    }

    @Override
    public void delete(@NotNull String key) {
        Assert.hasText(key, "Cache key must not be blank");
        REDIS.del(key);
        log.debug("Removed key: [{}]", key);
    }

    @PreDestroy
    public void preDestroy() {
    }
}
