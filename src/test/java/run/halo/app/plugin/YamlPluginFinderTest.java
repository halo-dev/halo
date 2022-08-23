package run.halo.app.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginRuntimeException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.util.InMemoryResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;
import run.halo.app.core.extension.Plugin;
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

    private File testFile;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        pluginFinder = new YamlPluginFinder();
        testFile = ResourceUtils.getFile("classpath:plugin/plugin.yaml");
    }

    @Test
    void find() throws IOException, JSONException {
        Path tempDirectory = Files.createTempDirectory("halo-test-plugin");

        Path directories = Files.createDirectories(tempDirectory.resolve("build/resources/main"));
        FileCopyUtils.copy(testFile, directories.resolve("plugin.yaml").toFile());

        Plugin plugin = pluginFinder.find(tempDirectory);
        assertThat(plugin).isNotNull();
        JSONAssert.assertEquals("""
                {
                    "phase": "RESOLVED"
                }
                """,
            JsonUtils.objectToJson(plugin.getStatus()),
            true);
    }

    @Test
    void findFromJar() throws FileNotFoundException {
        File file =
            ResourceUtils.getFile("classpath:plugin/test-unstructured-resource-loader.jar");
        Plugin plugin = pluginFinder.find(file.toPath());
        assertThat(plugin).isNotNull();
        assertThat(plugin.getMetadata().getName()).isEqualTo("io.github.guqing.apples");
    }

    @Test
    void unstructuredToPluginTest() throws JSONException {
        Plugin plugin = pluginFinder.unstructuredToPlugin(new FileSystemResource(testFile));
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
                        "license": [
                            {
                                "name": "MIT"
                            }
                        ],
                        "requires": ">=2.0.0",
                        "enabled": false
                    },
                    "apiVersion": "plugin.halo.run/v1alpha1",
                    "kind": "Plugin",
                    "metadata": {
                        "name": "plugin-1"
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
            .hasMessage("Unable to find plugin descriptor file: plugin.yaml");
    }

    @Test
    void acceptArrayObjectLicense() throws JSONException {
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
