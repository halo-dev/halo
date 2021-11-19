package run.halo.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import run.halo.app.cache.InMemoryCacheStore;
import run.halo.app.utils.JsonUtils;

/**
 * @author guqing
 * @since 2021-11-19
 */
public class HelloTest {
    InMemoryCacheStore cacheStore;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        cacheStore = new InMemoryCacheStore();
        cacheStore.putAny("ACCESS_PERMISSION: node018hxy3lbu6p1l1rl2l7o2nnssu1",
            Set.of("POST:3"), 1, TimeUnit.DAYS);

        cacheStore.putAny("ACCESS_PERMISSION: node01y2itoxr5jyw01k7ucq8a21re80",
            Set.of("POST:4", "POST:3"), 1, TimeUnit.DAYS);
    }

    @Test
    public void test() {
        for (Entry<String, String> entry : cacheStore.toMap().entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("ACCESS_PERMISSION: ")) {
                Set<String> valueSet = jsonToSet(entry.getValue());
                if (valueSet.contains("POST:3")) {
                    valueSet.remove("POST:3");
                    cacheStore.putAny(key, valueSet);
                }
            }
        }

        try {
            System.out.println(
                JsonUtils.objectToJson(cacheStore.toMap()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private Set<String> jsonToSet(String json) {
        try {
            return objectMapper.readValue(json,
                new TypeReference<LinkedHashSet<String>>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Collections.emptySet();
    }
}
