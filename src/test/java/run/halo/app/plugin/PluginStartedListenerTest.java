package run.halo.app.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

/**
 * Tests for {@link PluginStartedListener}.
 *
 * @author guqing
 * @since 2.0.0
 */
class PluginStartedListenerTest {

    @Nested
    class PluginExtensionLoaderUtilsTest {

        @Test
        void lookupFromClasses() throws IOException {
            Path tempPluginPath = Files.createTempDirectory("halo-test-plugin");

            Path directories =
                Files.createDirectories(tempPluginPath.resolve("build/resources/main"));
            Path extensions = Files.createDirectory(directories.resolve("extensions"));
            Files.createFile(extensions.resolve("roles.yaml"));

            Set<String> extensionResources =
                PluginStartedListener.PluginExtensionLoaderUtils.lookupFromClasses(tempPluginPath);
            assertThat(extensionResources).containsAll(Set.of("extensions/roles.yaml"));
        }

        @Test
        void lookupFromJar() throws FileNotFoundException {
            File file =
                ResourceUtils.getFile("classpath:plugin/test-unstructured-resource-loader.jar");
            Set<String> unstructuredFilePathFromJar =
                PluginStartedListener.PluginExtensionLoaderUtils.lookupFromJar(file.toPath());
            assertThat(unstructuredFilePathFromJar).hasSize(3);
            assertThat(unstructuredFilePathFromJar).containsAll(Set.of("extensions/roles.yaml",
                "extensions/reverseProxy.yaml", "extensions/test.yml"));
        }
    }
}