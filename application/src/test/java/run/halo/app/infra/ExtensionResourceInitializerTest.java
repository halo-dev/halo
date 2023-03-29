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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.FileSystemUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ReactiveExtensionClient;
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
    ReactiveExtensionClient extensionClient;
    @Mock
    HaloProperties haloProperties;
    @Mock
    SchemeInitializedEvent applicationReadyEvent;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    ExtensionResourceInitializer extensionResourceInitializer;

    List<Path> dirsToClean;

    @BeforeEach
    void setUp() throws IOException {
        dirsToClean = new ArrayList<>(2);

        Path tempDirectory = Files.createTempDirectory("extension-resource-initializer-test");
        dirsToClean.add(tempDirectory);
        Path multiDirectory =
            Files.createDirectories(tempDirectory.resolve("a").resolve("b").resolve("c"));
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

        // test file in directory
        Path secondTempDir = Files.createTempDirectory("extension-resource-file-test");
        dirsToClean.add(secondTempDir);
        Path filePath = secondTempDir.resolve("good.yml");
        Files.writeString(filePath, """
                kind: FakeExtension
                apiVersion: v1
                metadata:
                  name: config-file-is-ok
                spec:
                  key: value
                """,
            StandardCharsets.UTF_8);

        when(haloProperties.getInitialExtensionLocations())
            .thenReturn(Set.of("file:" + tempDirectory + "/**/*.yaml",
                "file:" + tempDirectory + "/**/*.yml",
                "file:" + filePath));
    }

    @AfterEach
    void cleanUp() throws IOException {
        if (dirsToClean != null) {
            for (var dir : dirsToClean) {
                FileSystemUtils.deleteRecursively(dir);
            }
        }
    }

    @Test
    void onApplicationEvent() throws JSONException {
        when(haloProperties.isRequiredExtensionDisabled()).thenReturn(true);
        var argumentCaptor = ArgumentCaptor.forClass(Unstructured.class);

        when(extensionClient.fetch(any(GroupVersionKind.class), any()))
            .thenReturn(Mono.empty());
        when(extensionClient.create(any())).thenReturn(Mono.empty());

        var initializeMono = extensionResourceInitializer.initialize(applicationReadyEvent);
        StepVerifier.create(initializeMono)
            .verifyComplete();


        verify(extensionClient, times(3)).create(argumentCaptor.capture());

        List<Unstructured> values = argumentCaptor.getAllValues();
        assertThat(values).isNotNull();
        assertThat(values).hasSize(3);
        JSONAssert.assertEquals("""
            [
                 {
                     "kind": "FakeExtension",
                     "apiVersion": "v1",
                     "metadata": {
                         "name": "config-file-is-ok"
                     },
                     "spec": {
                         "key": "value"
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