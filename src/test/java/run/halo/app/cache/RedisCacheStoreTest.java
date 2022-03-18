package run.halo.app.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.embedded.RedisServer;

/**
 * RedisCacheStoreTest.
 *
 * @author luoxx
 * @author guqing
 * @date 3/16/22
 */
@Slf4j
@SpringBootTest
class RedisCacheStoreTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    RedisCacheStore cacheStore;

    RedisServer redisServer;

    @BeforeEach
    void startRedis() {
        redisServer = RedisServer.builder()
            .port(6379)
            .build();
        redisServer.start();
        cacheStore = new RedisCacheStore(redisTemplate);
        clearAllCache();
    }


    @Test
    void putNullValueTest() {
        String key = "test_key";

        assertThrows(IllegalArgumentException.class, () -> cacheStore.put(key, null));
    }

    @Test
    void putNullKeyTest() {
        String value = "test_value";

        assertThrows(IllegalArgumentException.class, () -> cacheStore.put(null, value));
    }

    @Test
    void getByNullKeyTest() {
        assertThrows(IllegalArgumentException.class, () -> cacheStore.get(null));
    }

    @Test
    void getNullTest() {
        String key = "test_key";

        Optional<String> valueOptional = cacheStore.get(key);

        assertFalse(valueOptional.isPresent());
    }

    @Test
    void expirationTest() throws InterruptedException {
        String key = "test_key";
        String value = "test_value";
        cacheStore.put(key, value, 500, TimeUnit.MILLISECONDS);

        Optional<String> valueOptional = cacheStore.get(key);

        assertTrue(valueOptional.isPresent());
        assertEquals(value, valueOptional.get());

        TimeUnit.SECONDS.sleep(1L);

        valueOptional = cacheStore.get(key);

        assertFalse(valueOptional.isPresent());
    }

    @Test
    void deleteTest() {
        String key = "test_key";
        String value = "test_value";

        // Put the cache
        cacheStore.put(key, value);

        // Get the caceh
        Optional<String> valueOptional = cacheStore.get(key);

        // Assert
        assertTrue(valueOptional.isPresent());
        assertEquals(value, valueOptional.get());

        // Delete the cache
        cacheStore.delete(key);

        // Get the cache again
        valueOptional = cacheStore.get(key);

        // Assertion
        assertFalse(valueOptional.isPresent());
    }

    @Test
    void toMapTest() {
        String key1 = "test_key_1";
        String value1 = "test_value_1";

        // Put the cache
        cacheStore.put(key1, value1);
        LinkedHashMap<String, String> map = cacheStore.toMap();
        assertThat(map).isNotNull();
        assertThat(map.size()).isEqualTo(1);
        assertThat(map.get("halo.redis.test_key_1")).isEqualTo("test_value_1");

        String key2 = "test_key_2";
        String value2 = "test_value_2";

        // Put the cache
        cacheStore.put(key2, value2);

        map = cacheStore.toMap();
        assertThat(map).isNotNull();
        assertThat(map.size()).isEqualTo(2);
        assertThat(map.get("halo.redis.test_key_1")).isEqualTo("test_value_1");
        assertThat(map.get("halo.redis.test_key_1")).isEqualTo("test_value_1");
    }

    public void clearAllCache() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys == null) {
            return;
        }
        log.debug("Clear all cache.");
        redisTemplate.delete(keys);
    }

    @AfterEach
    void stopRedis() {
        redisServer.stop();
    }

}
