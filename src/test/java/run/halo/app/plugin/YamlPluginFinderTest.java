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
import org.springframework.util.ResourceUtils;
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
                    "license": "MIT",
                    "requires": ">=2.0.0",
                    "pluginClass": "run.halo.app.plugin.BasePlugin"
                },
                "apiVersion": "v1",
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
            false);
    }

    @Test
    void findFailedWhenFileNotFound() {
        Path test = Paths.get("/tmp");
        assertThatThrownBy(() -> {
            pluginFinder.find(test);
        }).isInstanceOf(PluginRuntimeException.class)
            .hasMessage("Cannot find '/tmp/plugin.yaml' path");
    }
}