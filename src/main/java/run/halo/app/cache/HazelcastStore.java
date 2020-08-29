package run.halo.app.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientConnectionStrategyConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.client.config.ConnectionRetryConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.utils.JsonUtils;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * hazelcast cache store
 * Create by turgay can on 2020/08/27 10:28
 */
@Slf4j
public class HazelcastStore extends AbstractStringCacheStore {

    private static final int ONE_SECOND_AS_MILLIS = 1000;
    private static final String DEFAULT_MAP = "haloMap";

    private HazelcastInstance hazelcastInstance;

    public HazelcastStore(HaloProperties haloProperties) {
        super.haloProperties = haloProperties;
    }

    @PostConstruct
    public void init() {
        if (hazelcastInstance != null) {
            return;
        }
        try {
            final ClientConfig config = new ClientConfig();
            final GroupConfig groupConfig = config.getGroupConfig();
            final String hazelcastGroupName = haloProperties.getHazelcastGroupName();
            groupConfig.setName(hazelcastGroupName);

            final ClientNetworkConfig network = config.getNetworkConfig();
            final List<String> hazelcastMembers = haloProperties.getHazelcastMembers();
            network.setAddresses(hazelcastMembers);

            configureClientRetryPolicy(config);

            log.info("Hazelcast client instance starting::GroupName={}::Members={}", hazelcastGroupName, hazelcastMembers);
            this.hazelcastInstance = HazelcastClient.newHazelcastClient(config);
            log.info("Hazelcast client instance started");
        } catch (Exception ex) {
            log.error("init hazelcast error ", ex);
        }
    }

    private void configureClientRetryPolicy(ClientConfig config) {
        ConnectionRetryConfig retryConfig = new ConnectionRetryConfig();
        retryConfig.setEnabled(true);
        retryConfig.setInitialBackoffMillis(haloProperties.getInitialBackoffSeconds() * ONE_SECOND_AS_MILLIS);

        config.getConnectionStrategyConfig()
                .setReconnectMode(ClientConnectionStrategyConfig.ReconnectMode.ON)
                .setConnectionRetryConfig(retryConfig);
    }

    @Override
    Optional<CacheWrapper<String>> getInternal(String key) {
        Assert.hasText(key, "Cache key must not be blank");
        final IMap<String, String> defaultHaloMap = getDefaultStringMap();
        final String v = defaultHaloMap.get(key);
        return StringUtils.isBlank(v) ? Optional.empty() : jsonToCacheWrapper(v);
    }

    @Override
    void putInternal(String key, CacheWrapper<String> cacheWrapper) {
        putInternalIfAbsent(key, cacheWrapper);
        try {
            getDefaultStringMap().set(key, JsonUtils.objectToJson(cacheWrapper));
            Date ttl = cacheWrapper.getExpireAt();
            if (ttl != null) {
                getDefaultStringMap().setTtl(key, ttl.getTime(), TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            log.warn("Put cache fail json2object key: [{}] value:[{}]", key, cacheWrapper);
        }
    }

    @Override
    Boolean putInternalIfAbsent(String key, CacheWrapper<String> cacheWrapper) {
        Assert.hasText(key, "Cache key must not be blank");
        Assert.notNull(cacheWrapper, "Cache wrapper must not be null");
        try {
            final IMap<String, String> defaultHaloMap = getDefaultStringMap();
            if (defaultHaloMap.containsKey(key)) {
                log.warn("Failed to put the cache, because the key: [{}] has been present already", key);
                return false;
            }
            Date ttl = cacheWrapper.getExpireAt();
            if (ttl != null) {
                defaultHaloMap.set(key, JsonUtils.objectToJson(cacheWrapper), ttl.getTime(), TimeUnit.MILLISECONDS);
            }
            return true;
        } catch (JsonProcessingException e) {
            log.warn("Put cache fail json2object key: [{}] value:[{}]", key, cacheWrapper);
        }
        log.debug("Cache key: [{}], original cache wrapper: [{}]", key, cacheWrapper);
        return false;
    }

    @Override
    public void delete(String key) {
        Assert.hasText(key, "Cache key must not be blank");
        final IMap<String, String> defaultHaloMap = getDefaultStringMap();
        defaultHaloMap.delete(key);
        log.debug("Removed key: [{}]", key);
    }

    private IMap<String, String> getDefaultStringMap() {
        return hazelcastInstance.getMap(DEFAULT_MAP);
    }
}
