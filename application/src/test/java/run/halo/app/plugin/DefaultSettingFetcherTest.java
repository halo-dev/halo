package run.halo.app.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link DefaultSettingFetcher}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class DefaultSettingFetcherTest {

    @Mock
    private ReactiveExtensionClient client;

    @MockitoBean
    private final PluginContext pluginContext = PluginContext.builder()
        .name("fake")
        .configMapName("fake-config")
        .build();

    @Mock
    private ApplicationContext applicationContext;

    private DefaultReactiveSettingFetcher reactiveSettingFetcher;
    private DefaultSettingFetcher settingFetcher;

    @BeforeEach
    void setUp() {
        this.reactiveSettingFetcher = new DefaultReactiveSettingFetcher(pluginContext, client);
        reactiveSettingFetcher.setApplicationContext(applicationContext);

        settingFetcher = new DefaultSettingFetcher(reactiveSettingFetcher);

        ConfigMap configMap = buildConfigMap();
        when(client.fetch(eq(ConfigMap.class), eq(pluginContext.getConfigMapName())))
            .thenReturn(Mono.just(configMap));
    }

    @Test
    void getValues() throws JSONException {
        Map<String, JsonNode> values = settingFetcher.getValues();

        verify(client, times(1)).fetch(eq(ConfigMap.class), any());

        assertThat(values).hasSize(2);
        JSONAssert.assertEquals(getSns(), JsonUtils.objectToJson(values.get("sns")), true);

        // The extensionClient will only be called once
        Map<String, JsonNode> callAgain = settingFetcher.getValues();
        assertThat(callAgain).isNotNull();
        verify(client, times(1)).fetch(eq(ConfigMap.class), any());
    }

    @Test
    void getValuesWithUpdateCache() throws JSONException {
        Map<String, JsonNode> values = settingFetcher.getValues();

        verify(client, times(1)).fetch(eq(ConfigMap.class), any());
        JSONAssert.assertEquals(getSns(), JsonUtils.objectToJson(values.get("sns")), true);

        ConfigMap configMap = buildConfigMap();
        configMap.getData().put("sns", """
            {
                "email": "abc@example.com",
                "github": "abc"
            }
            """);
        when(client.fetch(eq(ConfigMap.class), eq(pluginContext.getConfigMapName())))
            .thenReturn(Mono.just(configMap));
        when(client.update(configMap)).thenReturn(Mono.just(configMap));
        reactiveSettingFetcher.reconcile(new Reconciler.Request(pluginContext.getConfigMapName()));

        // Make sure the method cache#put is called before the event is published
        // to avoid the event listener to fetch the old value from the cache
        verify(applicationContext).publishEvent(isA(PluginConfigUpdatedEvent.class));

        Map<String, JsonNode> updatedValues = settingFetcher.getValues();
        verify(client, times(3)).fetch(eq(ConfigMap.class), any());
        assertThat(updatedValues).hasSize(2);
        JSONAssert.assertEquals(configMap.getData().get("sns"),
            JsonUtils.objectToJson(updatedValues.get("sns")), true);

        updatedValues = settingFetcher.getValues();
        assertThat(updatedValues).hasSize(2);
        verify(client, times(3)).fetch(eq(ConfigMap.class), any());
    }

    @Test
    void getGroupForObject() throws JSONException {
        Optional<Sns> sns = settingFetcher.fetch("sns", Sns.class);
        assertThat(sns.isEmpty()).isFalse();
        JSONAssert.assertEquals(getSns(), JsonUtils.objectToJson(sns.get()), true);
    }

    @Test
    void getGroup() {
        JsonNode jsonNode = settingFetcher.get("basic");
        assertThat(jsonNode).isNotNull();
        assertThat(jsonNode.isObject()).isTrue();
        assertThat(jsonNode.get("color").asText()).isEqualTo("red");
        assertThat(jsonNode.get("width").asInt()).isEqualTo(100);

        // missing key will return empty json node
        JsonNode emptyNode = settingFetcher.get("basic1");
        assertThat(emptyNode.isEmpty()).isTrue();
    }

    private ConfigMap buildConfigMap() {
        ConfigMap configMap = new ConfigMap();
        Metadata metadata = new Metadata();
        metadata.setName("fake");
        metadata.setLabels(Map.of("plugin.halo.run/plugin-name", "fake"));
        configMap.setMetadata(metadata);
        configMap.setKind("ConfigMap");
        configMap.setApiVersion("v1alpha1");
        var map = new HashMap<String, String>();
        map.put("sns", getSns());
        map.put("basic", """
            {
                "color": "red",
                "width": "100"
            }
            """);
        configMap.setData(map);
        return configMap;
    }

    String getSns() {
        return """
            {
                "email": "example@example.com",
                "github": "example",
                "instagram": "123",
                "twitter": "halo-dev",
                "user": {
                "name": "guqing",
                "age": "18"
                },
                "nums": [1, 2, 3]
            }
            """;
    }

    record Sns(String email, String github, String instagram, String twitter,
               Map<String, Object> user, List<Integer> nums) {
    }
}