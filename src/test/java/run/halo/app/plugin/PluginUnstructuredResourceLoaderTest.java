package run.halo.app.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

/**
 * Tests for {@link PluginUnstructuredResourceLoader}.
 *
 * @author guqing
 * @since 2.0.0
 */
class PluginUnstructuredResourceLoaderTest {

    private PluginUnstructuredResourceLoader unstructuredResourceLoader;

    @BeforeEach
    void setUp() {
        unstructuredResourceLoader = new PluginUnstructuredResourceLoader();
    }

    @Test
    void getUnstructuredFilePathFromJar() throws FileNotFoundException {
        File file = ResourceUtils.getFile("classpath:plugin/test-unstructured-resource-loader.jar");
        List<String> unstructuredFilePathFromJar =
            unstructuredResourceLoader.getUnstructuredFilePathFromJar(file.toPath());
        assertThat(unstructuredFilePathFromJar).hasSize(3);
        assertThat(unstructuredFilePathFromJar).contains("extensions/roles.yaml",
            "extensions/reverseProxy.yaml", "extensions/test.yml");
    }
}