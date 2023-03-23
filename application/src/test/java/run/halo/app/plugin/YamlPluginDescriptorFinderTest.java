package run.halo.app.plugin;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginDescriptor;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ResourceUtils;
import run.halo.app.infra.utils.FileUtils;
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
    private Path tempDirectory;

    @BeforeEach
    void setUp() throws IOException {
        yamlPluginDescriptorFinder = new YamlPluginDescriptorFinder();
        tempDirectory = Files.createTempDirectory("halo-plugin");
        var plugin002Uri = requireNonNull(
            ResourceUtils.getFile("classpath:plugin/plugin-0.0.2")).toURI();

        Path targetJarPath = tempDirectory.resolve("plugin-0.0.2.jar");
        FileUtils.jar(Paths.get(plugin002Uri), targetJarPath);
        testFile = targetJarPath.toFile();
    }

    @AfterEach
    void tearDown() throws IOException {
        FileSystemUtils.deleteRecursively(tempDirectory);
    }

    @Test
    void isApplicable() throws IOException {
        // File not exists
        boolean applicable =
            yamlPluginDescriptorFinder.isApplicable(Path.of("/some/path/test.jar"));
        assertThat(applicable).isFalse();

        // jar file is applicable
        Path tempJarFile = Files.createTempFile("test", ".jar");
        Path tempZipFile = Files.createTempFile("test", ".zip");
        try {
            applicable =
                yamlPluginDescriptorFinder.isApplicable(tempJarFile);
            assertThat(applicable).isTrue();
            // zip file is not applicable
            applicable =
                yamlPluginDescriptorFinder.isApplicable(tempZipFile);
            assertThat(applicable).isFalse();

            // directory is applicable
            applicable =
                yamlPluginDescriptorFinder.isApplicable(tempJarFile.getParent());
            assertThat(applicable).isTrue();
        } finally {
            FileUtils.deleteRecursivelyAndSilently(tempJarFile);
            FileUtils.deleteRecursivelyAndSilently(tempZipFile);
        }
    }

    @Test
    void find() throws JSONException {
        PluginDescriptor pluginDescriptor = yamlPluginDescriptorFinder.find(testFile.toPath());
        String actual = JsonUtils.objectToJson(pluginDescriptor);
        JSONAssert.assertEquals("""
                {
                     "pluginId": "fake-plugin",
                     "pluginDescription": "Fake description",
                     "pluginClass": "run.halo.app.plugin.BasePlugin",
                     "version": "0.0.2",
                     "requires": ">=2.0.0",
                     "provider": "johnniang",
                     "dependencies": [],
                     "license": "GPLv3"
                 }
                """,
            actual,
            false);
    }
}