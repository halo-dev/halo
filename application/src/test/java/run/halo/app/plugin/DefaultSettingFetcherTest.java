package run.halo.app.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
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
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
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
    private ReactiveExtensionClient extensionClient;

    private DefaultSettingFetcher settingFetcher;

    @BeforeEach
    void setUp() {
        DefaultReactiveSettingFetcher reactiveSettingFetcher =
            new DefaultReactiveSettingFetcher(extensionClient, "fake");
        settingFetcher = new DefaultSettingFetcher(reactiveSettingFetcher);
        // do not call extensionClient when the settingFetcher first time created
        verify(extensionClient, times(0)).fetch(eq(ConfigMap.class), any());
        verify(extensionClient, times(0)).fetch(eq(Plugin.class), any());

        Plugin plugin = buildPlugin();
        when(extensionClient.fetch(eq(Plugin.class), any())).thenReturn(Mono.just(plugin));

        ConfigMap configMap = buildConfigMap();
        when(extensionClient.fetch(eq(ConfigMap.class), any())).thenReturn(Mono.just(configMap));
    }

    @Test
    void getValues() throws JSONException {
        Map<String, JsonNode> values = settingFetcher.getValues();

        verify(extensionClient, times(1)).fetch(eq(ConfigMap.class), any());

        assertThat(values).hasSize(2);
        JSONAssert.assertEquals(getSns(), JsonUtils.objectToJson(values.get("sns")), true);

        // The extensionClient will only be called once
        Map<String, JsonNode> callAgain = settingFetcher.getValues();
        assertThat(callAgain).isNotNull();
    }

    @Test
    void getGroupForObject() throws JSONException {
        Optional<Sns> sns = settingFetcher.fetch("sns", Sns.class);
        assertThat(sns.isEmpty()).isFalse();
        JSONAssert.assertEquals(getSns(), JsonUtils.objectToJson(sns.get()), true);

        when(extensionClient.fetch(eq(ConfigMap.class), any())).thenReturn(Mono.empty());
        Optional<Sns> missing = settingFetcher.fetch("sns1", Sns.class);
        assertThat(missing.isEmpty()).isTrue();
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
        configMap.setData(Map.of("sns", getSns(),
            "basic", """
                {
                     "color": "red",
                     "width": "100"
                 }
                """)
        );
        return configMap;
    }

    private Plugin buildPlugin() {
        Plugin plugin = new Plugin();
        plugin.setKind("Plugin");
        plugin.setApiVersion("plugin.halo.run/v1alpha1");

        Metadata pluginMetadata = new Metadata();
        pluginMetadata.setName("fakePlugin");
        plugin.setMetadata(pluginMetadata);

        Plugin.PluginSpec pluginSpec = new Plugin.PluginSpec();
        pluginSpec.setConfigMapName("fakeConfigMap");
        pluginSpec.setSettingName("fakeSetting");
        plugin.setSpec(pluginSpec);
        return plugin;
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