package run.halo.app.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginRuntimeException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.core.io.Resource;
import org.springframework.security.util.InMemoryResource;
import org.springframework.util.ResourceUtils;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link YamlPluginDescriptorFinder}.
 *
 * @author guqing
 * @since 2.0.0
 */
class YamlPluginFinderTest {
    private YamlPluginFinder pluginFinder;
    private Path testPath;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        pluginFinder = new YamlPluginFinder();
        File file = ResourceUtils.getFile("classpath:plugin/plugin.yaml");
        testPath = file.toPath().getParent();
    }

    @Test
    void findTest() throws JsonProcessingException, JSONException {
        Plugin plugin = pluginFinder.find(testPath);
        assertThat(plugin).isNotNull();
        JSONAssert.assertEquals("""
                {
                    "spec": {
                        "displayName": "a name to show",
                        "version": "0.0.1",
                        "author": "guqing",
                        "logo": "https://guqing.xyz/avatar",
                        "pluginDependencies": {
                          "banana": "0.0.1"
                        },
                        "homepage": "https://github.com/guqing/halo-plugin-1",
                        "description": "Tell me more about this plugin.",
                        "license": [{
                            "name": "MIT",
                            "url": ""
                        }],
                        "requires": ">=2.0.0",
                        "pluginClass": "run.halo.app.plugin.BasePlugin"
                    },
                    "apiVersion": "plugin.halo.run/v1alpha1",
                    "kind": "Plugin",
                    "metadata": {
                        "name": "plugin-1",
                        "labels": null,
                        "annotations": null,
                        "version": null,
                        "creationTimestamp": null,
                        "deletionTimestamp": null
                    }
                }
                """,
            JsonUtils.objectToJson(plugin),
            true);
    }

    @Test
    void findFailedWhenFileNotFound() {
        Path test = Paths.get("/tmp");
        assertThatThrownBy(() -> {
            pluginFinder.find(test);
        }).isInstanceOf(PluginRuntimeException.class)
            .hasMessage("Cannot find '/tmp/plugin.yaml' path");
    }

    @Test
    void acceptArrayLicense() throws JSONException, JsonProcessingException {
        Resource pluginResource = new InMemoryResource("""
            apiVersion: v1
            kind: Plugin
            metadata:
              name: plugin-1
            spec:
              license: "MIT"
            """);
        Plugin plugin = pluginFinder.unstructuredToPlugin(pluginResource);
        assertThat(plugin.getSpec()).isNotNull();
        JSONAssert.assertEquals("""
            [{
                "name": "MIT",
                "url": ""
            }]
            """, JsonUtils.objectToJson(plugin.getSpec().getLicense()), false);
    }

    @Test
    void acceptMultipleItemArrayLicense() throws JsonProcessingException, JSONException {
        Resource pluginResource = new InMemoryResource("""
            apiVersion: v1
            kind: Plugin
            metadata:
              name: plugin-1
            spec:
              license: ["MIT", "Apache-2.0"]
            """);
        Plugin plugin = pluginFinder.unstructuredToPlugin(pluginResource);
        assertThat(plugin.getSpec()).isNotNull();
        JSONAssert.assertEquals("""
            [{
                "name": "MIT",
                "url": ""
            },
            {
                "name": "Apache-2.0",
                "url": ""
            }]
            """, JsonUtils.objectToJson(plugin.getSpec().getLicense()), false);
    }

    @Test
    void acceptArrayObjectLicense() throws JSONException, JsonProcessingException {
        Resource pluginResource = new InMemoryResource("""
            apiVersion: v1
            kind: Plugin
            metadata:
              name: plugin-1
            spec:
              license:
                - name: MIT
                  url: https://exmple.com
            """);
        Plugin plugin = pluginFinder.unstructuredToPlugin(pluginResource);
        assertThat(plugin.getSpec()).isNotNull();
        JSONAssert.assertEquals("""
            [{
                "name": "MIT",
                "url": "https://exmple.com"
            }]
            """, JsonUtils.objectToJson(plugin.getSpec().getLicense()), false);
    }

    @Test
    void deserializeLicense() throws JSONException, JsonProcessingException {
        String pluginJson = """
            {
                "apiVersion": "plugin.halo.run/v1alpha1",
                "kind": "Plugin",
                "metadata": {
                    "name": "plugin-1"
                },
                "spec": {
                    "license": [
                        {
                            "name": "MIT",
                            "url": "https://exmple.com"
                        }
                    ]
                }
            }
            """;
        Plugin plugin = Unstructured.OBJECT_MAPPER.readValue(pluginJson, Plugin.class);
        assertThat(plugin.getSpec()).isNotNull();
        JSONAssert.assertEquals(pluginJson, JsonUtils.objectToJson(plugin), false);
    }
}
