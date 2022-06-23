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

    private File testFile;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        yamlPluginDescriptorFinder = new YamlPluginDescriptorFinder();
        testFile = ResourceUtils.getFile("classpath:plugin/test-unstructured-resource-loader.jar");
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
        PluginDescriptor pluginDescriptor = yamlPluginDescriptorFinder.find(testFile.toPath());
        String actual = JsonUtils.objectToJson(pluginDescriptor);
        JSONAssert.assertEquals("""
                {
                     "pluginId": "io.github.guqing.apples",
                     "pluginDescription": "这是一个用来测试的插件",
                     "pluginClass": "run.halo.app.plugin.BasePlugin",
                     "version": "0.0.1",
                     "requires": "*",
                     "provider": "guqing",
                     "dependencies": [],
                     "license": "MIT"
                 }
                """,
            actual,
            false);
    }
}