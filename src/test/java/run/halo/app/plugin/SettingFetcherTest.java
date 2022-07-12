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
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link SettingFetcher}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class SettingFetcherTest {

    @Mock
    private ExtensionClient extensionClient;

    private SettingFetcher settingFetcher;

    @BeforeEach
    void setUp() {
        settingFetcher = new SettingFetcher("fake", extensionClient);
        // do not call extensionClient when the settingFetcher first time created
        verify(extensionClient, times(0)).fetch(eq(ConfigMap.class), any());
        verify(extensionClient, times(0)).fetch(eq(Plugin.class), any());

        Plugin plugin = buildPlugin();
        when(extensionClient.fetch(eq(Plugin.class), any())).thenReturn(Optional.of(plugin));

        ConfigMap configMap = buildConfigMap();
        when(extensionClient.fetch(eq(ConfigMap.class), any())).thenReturn(Optional.of(configMap));
    }

    @Test
    void getValues() throws JSONException {
        JsonNode values = settingFetcher.getValues();

        verify(extensionClient, times(1)).fetch(eq(ConfigMap.class), any());

        assertThat(values).hasSize(2);
        JSONAssert.assertEquals(getSns(), JsonUtils.objectToJson(values.get("sns")), true);

        // The extensionClient will only be called once
        JsonNode callAgain = settingFetcher.getValues();
        assertThat(callAgain).isNotNull();
        verify(extensionClient, times(1)).fetch(eq(ConfigMap.class), any());
    }

    @Test
    void getGroupForObject() throws JSONException {
        Sns sns = settingFetcher.getGroupForObject("sns", Sns.class);
        assertThat(sns).isNotNull();
        JSONAssert.assertEquals(getSns(), JsonUtils.objectToJson(sns), true);

        Sns missing = settingFetcher.getGroupForObject("sns1", Sns.class);
        assertThat(missing).isNull();
    }

    @Test
    void getGroup() {
        JsonNode jsonNode = settingFetcher.getGroup("basic");
        assertThat(jsonNode).isNotNull();
        assertThat(jsonNode.isObject()).isTrue();
        assertThat(jsonNode.get("color").asText()).isEqualTo("red");
        assertThat(jsonNode.get("width").asInt()).isEqualTo(100);

        // missing key will return empty json node
        JsonNode emptyNode = settingFetcher.getGroup("basic1");
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
        configMap.setData(Map.of("setting", String.format("""
            {
                 "sns": %s,
                 "basic": {
                     "color": "red",
                     "width": "100"
                 }
            }
            """, getSns())));
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