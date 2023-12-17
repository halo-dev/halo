package run.halo.app.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.util.InMemoryResource;
import run.halo.app.infra.utils.YamlUnstructuredLoader;

/**
 * Tests for {@link ConfigMap}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class ConfigMapTest {

    @Mock
    ExtensionClient extensionClient;

    @Test
    void configMapTest() {
        ArgumentCaptor<ConfigMap> argumentCaptor = ArgumentCaptor.forClass(ConfigMap.class);
        doNothing().when(extensionClient).create(argumentCaptor.capture());

        ConfigMap configMap = new ConfigMap();
        Metadata metadata = new Metadata();
        metadata.setName("test-configmap");
        configMap.setMetadata(metadata);
        Map<String, String> data = Map.of("k1", "v1", "k2", "v2", "k3", "v3");
        configMap.setData(data);
        extensionClient.create(configMap);

        ConfigMap value = argumentCaptor.getValue();
        assertThat(value).isNotNull();
        assertThat(value.getData()).isEqualTo(data);
    }

    @Test
    void putDataItem() {
        ConfigMap configMap = new ConfigMap();
        configMap.putDataItem("k1", "v1")
            .putDataItem("k2", "v2")
            .putDataItem("k3", "v3");

        assertThat(configMap.getData()).isNotNull();
        assertThat(configMap.getData()).hasSize(3);
        assertThat(configMap.getData()).isEqualTo(
            Map.of("k1", "v1", "k2", "v2", "k3", "v3"));
    }

    @Test
    void equalsTest() {
        ConfigMap configMapA = new ConfigMap();
        Metadata metadataA = new Metadata();
        metadataA.setName("test-configmap");
        configMapA.setMetadata(metadataA);
        configMapA.putDataItem("k1", "v1");

        ConfigMap configMapB = new ConfigMap();
        Metadata metadataB = new Metadata();
        metadataB.setName("test-configmap");
        configMapB.setMetadata(metadataB);
        configMapB.putDataItem("k1", "v1");

        assertThat(configMapA).isEqualTo(configMapB);

        configMapB.getMetadata().setName("test-configmap-2");
        assertThat(configMapA).isNotEqualTo(configMapB);
    }

    @Test
    void yamlTest() {
        String configMapYaml = """
                apiVersion: v1alpha1
                kind: ConfigMap
                metadata:
                  name: test-configmap
                data:
                  k1: v1
                  k2: v2
                  k3: v3
            """;
        List<Unstructured> unstructureds =
            new YamlUnstructuredLoader(new InMemoryResource(configMapYaml)).load();
        assertThat(unstructureds).hasSize(1);
        Unstructured unstructured = unstructureds.get(0);
        ConfigMap configMap =
            Unstructured.OBJECT_MAPPER.convertValue(unstructured, ConfigMap.class);

        assertThat(configMap.getData()).isEqualTo(Map.of("k1", "v1", "k2", "v2", "k3", "v3"));
        assertThat(configMap.getMetadata().getName()).isEqualTo("test-configmap");
        assertThat(configMap.getApiVersion()).isEqualTo("v1alpha1");
        assertThat(configMap.getKind()).isEqualTo("ConfigMap");
    }
}