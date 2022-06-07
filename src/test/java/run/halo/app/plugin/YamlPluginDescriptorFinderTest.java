package run.halo.app.plugin;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginDescriptor;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.util.ResourceUtils;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link YamlPluginDescriptorFinder}.
 *
 * @author guqing
 * @since 2.0.0
 */
class YamlPluginDescriptorFinderTest {

    private YamlPluginDescriptorFinder yamlPluginDescriptorFinder;
    private Path testPath;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        yamlPluginDescriptorFinder = new YamlPluginDescriptorFinder();
        File file = ResourceUtils.getFile("classpath:plugin/plugin.yaml");
        testPath = file.toPath().getParent();
    }

    @Test
    void isApplicable() throws IOException {
        // File not exists
        boolean applicable =
            yamlPluginDescriptorFinder.isApplicable(Path.of("/some/path/test.jar"));
        assertThat(applicable).isFalse();

        // jar file is applicable
        Path tempJarFile = Files.createTempFile("test", ".jar");
        applicable =
            yamlPluginDescriptorFinder.isApplicable(tempJarFile);
        assertThat(applicable).isTrue();

        // zip file is not applicable
        Path tempZipFile = Files.createTempFile("test", ".zip");
        applicable =
            yamlPluginDescriptorFinder.isApplicable(tempZipFile);
        assertThat(applicable).isFalse();

        // directory is applicable
        applicable =
            yamlPluginDescriptorFinder.isApplicable(tempJarFile.getParent());
        assertThat(applicable).isTrue();
    }

    @Test
    void find() throws JsonProcessingException, JSONException {
        PluginDescriptor pluginDescriptor = yamlPluginDescriptorFinder.find(testPath);
        String actual = JsonUtils.objectToJson(pluginDescriptor);
        JSONAssert.assertEquals("""
                {
                    "pluginId": "plugin-1",
                    "pluginDescription": "Tell me more about this plugin.",
                    "pluginClass": "run.halo.app.plugin.BasePlugin",
                    "version": "0.0.1",
                    "requires": ">=2.0.0",
                    "provider": "guqing",
                    "dependencies": [
                        {
                            "pluginId": "banana",
                            "pluginVersionSupport": "0.0.1",
                            "optional": false
                        }
                    ],
                    "license": "MIT"
                }
                """,
            actual,
            false);
    }
}