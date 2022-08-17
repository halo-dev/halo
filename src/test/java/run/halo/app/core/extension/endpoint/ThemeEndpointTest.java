package run.halo.app.core.extension.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.YamlUnstructuredLoader;

/**
 * Tests for {@link ThemeEndpoint}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class ThemeEndpointTest {

    @Mock
    private ReactiveExtensionClient extensionClient;

    @Mock
    private HaloProperties haloProperties;

    private Path tmpHaloWorkDir;

    WebTestClient webTestClient;

    private File defaultTheme;

    @BeforeEach
    void setUp() throws IOException {
        tmpHaloWorkDir = Files.createTempDirectory("halo-unit-test");

        ThemeEndpoint themeEndpoint = new ThemeEndpoint(extensionClient, haloProperties);

        when(haloProperties.getWorkDir()).thenReturn(tmpHaloWorkDir);

        defaultTheme = ResourceUtils.getFile("classpath:themes/test-theme.zip");

        webTestClient = WebTestClient
            .bindToRouterFunction(themeEndpoint.endpoint())
            .build();
    }

    @AfterEach
    void tearDown() {
        FileSystemUtils.deleteRecursively(tmpHaloWorkDir.toFile());
    }

    @Test
    void install() {
        when(extensionClient.create(any(Unstructured.class))).thenReturn(
            Mono.fromCallable(() -> {
                var defaultThemeManifestPath = tmpHaloWorkDir.resolve("themes/default/theme.yaml");
                assertThat(Files.exists(defaultThemeManifestPath)).isTrue();
                return new YamlUnstructuredLoader(new FileSystemResource(defaultThemeManifestPath))
                    .load()
                    .get(0);
            })).thenReturn(Mono.empty()).thenReturn(Mono.empty());

        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", new FileSystemResource(defaultTheme))
            .contentType(MediaType.MULTIPART_FORM_DATA);

        webTestClient.post()
            .uri("/themes/install")
            .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(Theme.class)
            .value(theme -> {
                verify(extensionClient, times(1)).create(any(Unstructured.class));

                assertThat(theme).isNotNull();
                assertThat(theme.getMetadata().getName()).isEqualTo("default");
            });

        // Verify the theme is installed.
        webTestClient.post()
            .uri("/themes/install")
            .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
            .exchange()
            .expectStatus()
            .is5xxServerError();
    }
}