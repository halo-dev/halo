package run.halo.app.infra.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class SystemConfigUtilsTest {

    private final JsonMapper mapper = JacksonUtils.MAPPER;

    @Test
    void mergeMapShouldMergeEmptyMaps() {
        Map<String, String> defaultMap = new HashMap<>();
        Map<String, String> overrideMap = new HashMap<>();

        Map<String, String> result = SystemConfigUtils.mergeMap(defaultMap, overrideMap);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void mergeMapShouldReturnDefaultMapWhenOverrideIsEmpty() {
        Map<String, String> defaultMap = new HashMap<>();
        defaultMap.put("group1", "{\"key1\":\"value1\"}");
        Map<String, String> overrideMap = new HashMap<>();

        Map<String, String> result = SystemConfigUtils.mergeMap(defaultMap, overrideMap);

        assertEquals(1, result.size());
        assertEquals("{\"key1\":\"value1\"}", result.get("group1"));
    }

    @Test
    void mergeMapShouldAddNewKeysFromOverrideMap() {
        Map<String, String> defaultMap = new HashMap<>();
        defaultMap.put("group1", "{\"key1\":\"value1\"}");
        Map<String, String> overrideMap = new HashMap<>();
        overrideMap.put("group2", "{\"key2\":\"value2\"}");

        Map<String, String> result = SystemConfigUtils.mergeMap(defaultMap, overrideMap);

        assertEquals(2, result.size());
        assertEquals("{\"key1\":\"value1\"}", result.get("group1"));
        assertEquals("{\"key2\":\"value2\"}", result.get("group2"));
    }

    @Test
    void mergeMapShouldDeepMergeJsonObjects() {
        Map<String, String> defaultMap = new HashMap<>();
        defaultMap.put("group1", "{\"key1\":\"value1\",\"nested\":{\"a\":\"1\"}}");
        Map<String, String> overrideMap = new HashMap<>();
        overrideMap.put("group1", "{\"key2\":\"value2\",\"nested\":{\"b\":\"2\"}}");

        Map<String, String> result = SystemConfigUtils.mergeMap(defaultMap, overrideMap);

        assertEquals(1, result.size());
        JsonNode resultNode = mapper.readTree(result.get("group1"));
        assertEquals("value1", resultNode.get("key1").asString());
        assertEquals("value2", resultNode.get("key2").asString());
        assertEquals("1", resultNode.get("nested").get("a").asString());
        assertEquals("2", resultNode.get("nested").get("b").asString());
    }

    @Test
    void mergeMapShouldOverrideExistingValues() {
        Map<String, String> defaultMap = new HashMap<>();
        defaultMap.put("group1", "{\"key1\":\"oldValue\"}");
        Map<String, String> overrideMap = new HashMap<>();
        overrideMap.put("group1", "{\"key1\":\"newValue\"}");

        Map<String, String> result = SystemConfigUtils.mergeMap(defaultMap, overrideMap);

        assertEquals(1, result.size());
        JsonNode resultNode = mapper.readTree(result.get("group1"));
        assertEquals("newValue", resultNode.get("key1").asString());
    }

    @Test
    void mergeMapShouldHandleComplexNestedStructures() {
        Map<String, String> defaultMap = new HashMap<>();
        defaultMap.put("config", "{\"server\":{\"port\":8080,\"host\":\"localhost\"},\"features\":{\"auth\":true}}");
        Map<String, String> overrideMap = new HashMap<>();
        overrideMap.put("config", "{\"server\":{\"port\":9090},\"features\":{\"logging\":true}}");

        Map<String, String> result = SystemConfigUtils.mergeMap(defaultMap, overrideMap);

        JsonNode resultNode = mapper.readTree(result.get("config"));
        assertEquals(9090, resultNode.get("server").get("port").asInt());
        assertEquals("localhost", resultNode.get("server").get("host").asString());
        assertTrue(resultNode.get("features").get("auth").asBoolean());
        assertTrue(resultNode.get("features").get("logging").asBoolean());
    }

    @Test
    void mergeMapShouldHandleArrayReplacement() {
        Map<String, String> defaultMap = new HashMap<>();
        defaultMap.put("group1", "{\"items\":[1,2,3]}");
        Map<String, String> overrideMap = new HashMap<>();
        overrideMap.put("group1", "{\"items\":[4,5,6]}");

        Map<String, String> result = SystemConfigUtils.mergeMap(defaultMap, overrideMap);

        JsonNode resultNode = mapper.readTree(result.get("group1"));
        JsonNode items = resultNode.get("items");
        assertEquals(3, items.size());
        assertEquals(4, items.get(0).asInt());
        assertEquals(5, items.get(1).asInt());
        assertEquals(6, items.get(2).asInt());
    }

    @Test
    void mergeMapShouldHandleNullValues() {
        Map<String, String> defaultMap = new HashMap<>();
        defaultMap.put("group1", "{\"key1\":\"value1\"}");
        Map<String, String> overrideMap = new HashMap<>();
        overrideMap.put("group1", "{\"key1\":null}");

        Map<String, String> result = SystemConfigUtils.mergeMap(defaultMap, overrideMap);

        JsonNode resultNode = mapper.readTree(result.get("group1"));
        assertTrue(resultNode.get("key1").isNull());
    }

    @Test
    void mergeMapShouldThrowExceptionForInvalidJson() {
        Map<String, String> defaultMap = new HashMap<>();
        defaultMap.put("group1", "invalid json");
        Map<String, String> overrideMap = new HashMap<>();
        overrideMap.put("group1", "{\"key1\":\"value1\"}");

        assertThrows(JacksonException.class, () -> SystemConfigUtils.mergeMap(defaultMap, overrideMap));
    }

    @Test
    void mergeConfigMapShouldMergeConfigMaps() {
        ConfigMap defaultConfigMap = new ConfigMap();
        Metadata defaultMetadata = new Metadata();
        defaultMetadata.setName("default-config");
        defaultConfigMap.setMetadata(defaultMetadata);
        Map<String, String> defaultData = new HashMap<>();
        defaultData.put("group1", "{\"key1\":\"value1\"}");
        defaultConfigMap.setData(defaultData);

        ConfigMap overrideConfigMap = new ConfigMap();
        Metadata overrideMetadata = new Metadata();
        overrideMetadata.setName("override-config");
        overrideConfigMap.setMetadata(overrideMetadata);
        Map<String, String> overrideData = new HashMap<>();
        overrideData.put("group1", "{\"key2\":\"value2\"}");
        overrideConfigMap.setData(overrideData);

        ConfigMap result = SystemConfigUtils.mergeConfigMap(defaultConfigMap, overrideConfigMap);

        assertNotNull(result);
        assertEquals("override-config", result.getMetadata().getName());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        JsonNode resultNode = mapper.readTree(result.getData().get("group1"));
        assertEquals("value1", resultNode.get("key1").asString());
        assertEquals("value2", resultNode.get("key2").asString());
    }

    @Test
    void mergeConfigMapShouldHandleNullData() {
        ConfigMap defaultConfigMap = new ConfigMap();
        Metadata defaultMetadata = new Metadata();
        defaultMetadata.setName("default-config");
        defaultConfigMap.setMetadata(defaultMetadata);
        defaultConfigMap.setData(null);

        ConfigMap overrideConfigMap = new ConfigMap();
        Metadata overrideMetadata = new Metadata();
        overrideMetadata.setName("override-config");
        overrideConfigMap.setMetadata(overrideMetadata);
        Map<String, String> overrideData = new HashMap<>();
        overrideData.put("group1", "{\"key1\":\"value1\"}");
        overrideConfigMap.setData(overrideData);

        ConfigMap result = SystemConfigUtils.mergeConfigMap(defaultConfigMap, overrideConfigMap);

        assertNotNull(result);
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals("{\"key1\":\"value1\"}", result.getData().get("group1"));
    }

    @Test
    void mergeConfigMapShouldHandleBothNullData() {
        ConfigMap defaultConfigMap = new ConfigMap();
        Metadata defaultMetadata = new Metadata();
        defaultMetadata.setName("default-config");
        defaultConfigMap.setMetadata(defaultMetadata);
        defaultConfigMap.setData(null);

        ConfigMap overrideConfigMap = new ConfigMap();
        Metadata overrideMetadata = new Metadata();
        overrideMetadata.setName("override-config");
        overrideConfigMap.setMetadata(overrideMetadata);
        overrideConfigMap.setData(null);

        ConfigMap result = SystemConfigUtils.mergeConfigMap(defaultConfigMap, overrideConfigMap);

        assertNotNull(result);
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void mergeConfigMapShouldUseOverrideMetadata() {
        ConfigMap defaultConfigMap = new ConfigMap();
        Metadata defaultMetadata = new Metadata();
        defaultMetadata.setName("default-config");
        defaultConfigMap.setMetadata(defaultMetadata);
        defaultConfigMap.setData(new HashMap<>());

        ConfigMap overrideConfigMap = new ConfigMap();
        Metadata overrideMetadata = new Metadata();
        overrideMetadata.setName("override-config");
        overrideConfigMap.setMetadata(overrideMetadata);
        overrideConfigMap.setData(new HashMap<>());

        ConfigMap result = SystemConfigUtils.mergeConfigMap(defaultConfigMap, overrideConfigMap);

        assertEquals("override-config", result.getMetadata().getName());
    }

    @Test
    void mergeMapShouldHandlePrimitiveValueReplacement() {
        Map<String, String> defaultMap = new HashMap<>();
        defaultMap.put("group1", "{\"number\":42,\"boolean\":true,\"string\":\"old\"}");
        Map<String, String> overrideMap = new HashMap<>();
        overrideMap.put("group1", "{\"number\":99,\"boolean\":false,\"string\":\"new\"}");

        Map<String, String> result = SystemConfigUtils.mergeMap(defaultMap, overrideMap);

        JsonNode resultNode = mapper.readTree(result.get("group1"));
        assertEquals(99, resultNode.get("number").asInt());
        assertFalse(resultNode.get("boolean").asBoolean());
        assertEquals("new", resultNode.get("string").asString());
    }

    @Test
    void mergeMapShouldHandleEmptyJsonObjects() {
        Map<String, String> defaultMap = new HashMap<>();
        defaultMap.put("group1", "{}");
        Map<String, String> overrideMap = new HashMap<>();
        overrideMap.put("group1", "{\"key1\":\"value1\"}");

        Map<String, String> result = SystemConfigUtils.mergeMap(defaultMap, overrideMap);

        JsonNode resultNode = mapper.readTree(result.get("group1"));
        assertEquals("value1", resultNode.get("key1").asString());
    }

    @Test
    void mergeMapShouldHandleDeepNesting() {
        Map<String, String> defaultMap = new HashMap<>();
        defaultMap.put("group1", "{\"level1\":{\"level2\":{\"level3\":{\"key\":\"value1\"}}}}");
        Map<String, String> overrideMap = new HashMap<>();
        overrideMap.put("group1", "{\"level1\":{\"level2\":{\"level3\":{\"newKey\":\"value2\"}}}}");

        Map<String, String> result = SystemConfigUtils.mergeMap(defaultMap, overrideMap);

        JsonNode resultNode = mapper.readTree(result.get("group1"));
        JsonNode level3 = resultNode.get("level1").get("level2").get("level3");
        assertEquals("value1", level3.get("key").asString());
        assertEquals("value2", level3.get("newKey").asString());
    }
}
