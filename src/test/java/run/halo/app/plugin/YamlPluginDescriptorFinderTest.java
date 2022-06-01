package run.halo.app.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginDescriptor;
import org.springframework.util.ResourceUtils;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link YamlPluginDescriptorFinder}.
 *
 * @author guqing
 * @since 2.0.0
 */
class YamlPluginDescriptorFinderTest {
    private YamlPluginDescriptorFinder descriptorFinder;
    private Path testPath;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        descriptorFinder = new YamlPluginDescriptorFinder();
        File file = ResourceUtils.getFile("classpath:plugin/plugin.yaml");
        testPath = file.toPath().getParent();
    }

    @Test
    void findTest() throws JsonProcessingException {
        PluginDescriptor pluginDescriptor = descriptorFinder.find(testPath);
        assertThat(pluginDescriptor).isNotNull();
        assertThat(JsonUtils.objectToJson(pluginDescriptor)).isEqualToIgnoringWhitespace("""
            {
                "spec": {
                    "displayName": "a name to show",
                    "version": "0.0.1",
                    "author": "guqing",
                    "logo": "https://guqing.xyz/avatar",
                    "dependencies": [],
                    "homepage": "https://github.com/guqing/halo-plugin-1",
                    "description": "Tell me more about this plugin.",
                    "license": "MIT",
                    "requires": ">=2.0.0",
                    "pluginClass": "org.pf4j.Plugin"
                },
                "dependencies": [],
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
            """);
    }

    @Test
    void findFailedWhenFileNotFound() {
        Path test = testPath.resolve("tmp");
        assertThatThrownBy(() -> {
            descriptorFinder.find(test);
        });
    }
}