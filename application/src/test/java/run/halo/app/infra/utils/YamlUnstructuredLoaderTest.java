package run.halo.app.infra.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.security.util.InMemoryResource;
import run.halo.app.extension.Unstructured;

/**
 * Tests for {@link YamlUnstructuredLoader}.
 *
 * @author guqing
 * @since 2.0.0
 */
class YamlUnstructuredLoaderTest {

    private List<InMemoryResource> yamlResources;
    private String notSpecYaml;

    @BeforeEach
    void setUp() {
        String viewCategoriesRoleYaml = """
            apiVersion: v1alpha1
            kind: Fake
            metadata:
              name: test1
            hello:
              world: halo
            """;

        String multipleRoleYaml = """
            apiVersion: v1alpha1
            kind: Fake
            metadata:
              name: test2
            hello:
              world: haha
            ---
            apiVersion: v1alpha1
            kind: Fake
            metadata:
              name: test2
            hello:
              world: bang
            """;

        notSpecYaml = """
            server:
              port: 8090
            spring:
              jackson:
                date-format: yyyy-MM-dd HH:mm:ss
            """;

        yamlResources = Stream.of(viewCategoriesRoleYaml, multipleRoleYaml, notSpecYaml)
            .map(InMemoryResource::new)
            .toList();
    }

    @Test
    void loadTest() {
        Resource[] resources = yamlResources.toArray(Resource[]::new);
        YamlUnstructuredLoader yamlUnstructuredLoader = new YamlUnstructuredLoader(resources);
        List<Unstructured> unstructuredList = yamlUnstructuredLoader.load();
        assertThat(unstructuredList).isNotNull();
        assertThat(unstructuredList).hasSize(3);

        assertThat(JsonUtils.objectToJson(unstructuredList)).isEqualToIgnoringWhitespace("""
            [
                 {
                     "apiVersion": "v1alpha1",
                     "kind": "Fake",
                     "metadata": {
                         "name": "test1"
                     },
                     "hello": {
                         "world": "halo"
                     }
                 },
                 {
                     "apiVersion": "v1alpha1",
                     "kind": "Fake",
                     "metadata": {
                         "name": "test2"
                     },
                     "hello": {
                         "world": "haha"
                     }
                 },
                 {
                     "apiVersion": "v1alpha1",
                     "kind": "Fake",
                     "metadata": {
                         "name": "test2"
                     },
                     "hello": {
                         "world": "bang"
                     }
                 }
             ]
            """);
    }

    @Test
    void loadIgnore() {
        InMemoryResource resource = new InMemoryResource(notSpecYaml);
        YamlUnstructuredLoader yamlUnstructuredLoader = new YamlUnstructuredLoader(resource);
        List<Unstructured> unstructuredList = yamlUnstructuredLoader.load();
        assertThat(unstructuredList).isEmpty();
    }

}