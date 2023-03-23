package run.halo.app.plugin;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ResourceUtils;
import run.halo.app.infra.utils.FileUtils;

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
                PluginExtensionLoaderUtils.lookupFromClasses(tempPluginPath);
            assertThat(extensionResources)
                .containsAll(Set.of(Path.of("extensions/roles.yaml").toString()));
        }

        @Test
        void lookupFromJar() throws IOException {
            Path tempDirectory = Files.createTempDirectory("halo-plugin");
            try {
                var plugin001Uri = requireNonNull(
                    ResourceUtils.getFile("classpath:plugin/plugin-0.0.1")).toURI();

                Path targetJarPath = tempDirectory.resolve("plugin-0.0.1.jar");
                FileUtils.jar(Paths.get(plugin001Uri), targetJarPath);
                Set<String> unstructuredFilePathFromJar =
                    PluginExtensionLoaderUtils.lookupFromJar(targetJarPath);
                assertThat(unstructuredFilePathFromJar).hasSize(3);
                assertThat(unstructuredFilePathFromJar).containsAll(Set.of(
                    Path.of("extensions/roles.yaml").toString(),
                    Path.of("extensions/reverseProxy.yaml").toString(),
                    Path.of("extensions/test.yml").toString()));
            } finally {
                FileSystemUtils.deleteRecursively(tempDirectory);
            }
        }
    }
}