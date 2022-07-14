package run.halo.app.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link ExtensionResourceInitializer}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class ExtensionResourceInitializerTest {

    @Mock
    private ExtensionClient extensionClient;
    @Mock
    private HaloProperties haloProperties;
    @Mock
    private ApplicationReadyEvent applicationReadyEvent;

    private ExtensionResourceInitializer extensionResourceInitializer;

    @BeforeEach
    void setUp() throws IOException {
        extensionResourceInitializer =
            new ExtensionResourceInitializer(haloProperties, extensionClient);
        Path tempDirectory = Files.createTempDirectory("extension-resource-initializer-test");

        when(haloProperties.getInitialExtensionLocations()).thenReturn(
            Set.of(tempDirectory.toString()));
        Path multiDirectory = Files.createDirectories(tempDirectory.resolve("a/b/c"));
        Files.writeString(tempDirectory.resolve("hello.yml"), """
                kind: FakeExtension
                apiVersion: v1
                metadata:
                  name: fake-extension
                spec:
                  hello: world
                """,
            StandardCharsets.UTF_8);
        Files.writeString(multiDirectory.getParent().resolve("fake-1.txt"), """
                kind: FakeExtension
                name: fake-extension
                """,
            StandardCharsets.UTF_8);
        Files.writeString(multiDirectory.resolve("fake.yaml"), """
                kind: FakeExtension
                apiVersion: v1
                metadata:
                  name: fake-extension
                spec:
                  hello: world
                """,
            StandardCharsets.UTF_8);
        when(haloProperties.getInitialExtensionLocations())
            .thenReturn(Set.of(tempDirectory.toString()));
    }

    @Test
    void onApplicationEvent() throws JSONException {
        ArgumentCaptor<Unstructured> argumentCaptor = ArgumentCaptor.forClass(Unstructured.class);

        when(extensionClient.fetch(any(GroupVersionKind.class), any())).thenReturn(
            Optional.empty());

        extensionResourceInitializer.onApplicationEvent(applicationReadyEvent);

        verify(extensionClient, times(2)).create(argumentCaptor.capture());

        List<Unstructured> values = argumentCaptor.getAllValues();
        assertThat(values).isNotNull();
        assertThat(values).hasSize(2);
        JSONAssert.assertEquals("""
            [
                {
                    "kind": "FakeExtension",
                    "apiVersion": "v1",
                    "metadata": {
                        "name": "fake-extension"
                    },
                    "spec": {
                        "hello": "world"
                    }
                },
                {
                    "kind": "FakeExtension",
                    "apiVersion": "v1",
                    "metadata": {
                        "name": "fake-extension"
                    },
                    "spec": {
                        "hello": "world"
                    }
                }
            ]
            """, JsonUtils.objectToJson(values), false);
    }
}