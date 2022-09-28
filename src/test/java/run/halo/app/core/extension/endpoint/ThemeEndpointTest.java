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
import java.util.List;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.JsonUtils;
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
        when(haloProperties.getWorkDir()).thenReturn(tmpHaloWorkDir);

        ThemeEndpoint themeEndpoint = new ThemeEndpoint(extensionClient, haloProperties);

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

    @Test
    void reloadSetting() throws IOException {
        Theme theme = new Theme();
        theme.setMetadata(new Metadata());
        theme.getMetadata().setName("fake-theme");
        theme.setSpec(new Theme.ThemeSpec());
        theme.getSpec().setDisplayName("Hello");
        theme.getSpec().setSettingName("fake-setting");
        when(extensionClient.fetch(Theme.class, "fake-theme"))
            .thenReturn(Mono.just(theme));
        Setting setting = new Setting();
        setting.setMetadata(new Metadata());
        setting.setSpec(new Setting.SettingSpec());
        setting.getSpec().setForms(List.of());
        when(extensionClient.fetch(Setting.class, "fake-setting"))
            .thenReturn(Mono.just(setting));

        when(haloProperties.getWorkDir()).thenReturn(tmpHaloWorkDir);
        Path themeWorkDir = tmpHaloWorkDir.resolve("themes")
            .resolve(theme.getMetadata().getName());
        if (!Files.exists(themeWorkDir)) {
            Files.createDirectories(themeWorkDir);
        }
        Files.writeString(themeWorkDir.resolve("settings.yaml"), """
            apiVersion: v1alpha1
            kind: Setting
            metadata:
              name: fake-setting
            spec:
              forms:
                - group: sns
                  label: 社交资料
                  formSchema:
                    - $el: h1
                      children: Register
            """);

        Files.writeString(themeWorkDir.resolve("theme.yaml"), """
            apiVersion: v1alpha1
            kind: Theme
            metadata:
              name: fake-theme
            spec:
              displayName: Fake Theme
            """);
        when(extensionClient.update(any(Theme.class)))
            .thenReturn(Mono.just(theme));

        when(extensionClient.update(any(Setting.class)))
            .thenReturn(Mono.just(setting));
        ArgumentCaptor<AbstractExtension> captor = ArgumentCaptor.forClass(Setting.class);
        webTestClient.put()
            .uri("/themes/fake-theme/reload-setting")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(Setting.class)
            .value(settingRes -> {
                verify(extensionClient, times(2)).update(captor.capture());
                verify(extensionClient, times(0)).create(any(Setting.class));
                List<AbstractExtension> allValues = captor.getAllValues();
                assertThat(allValues.get(0)).isInstanceOfAny(Setting.class);
                Setting newSetting = (Setting) allValues.get(0);
                Theme newTheme = (Theme) allValues.get(1);
                try {
                    JSONAssert.assertEquals("""
                            {
                               "spec": {
                                 "forms": [
                                   {
                                     "group": "sns",
                                     "label": "社交资料",
                                     "formSchema": [
                                       {
                                         "$el": "h1",
                                         "children": "Register"
                                       }
                                     ]
                                   }
                                 ]
                               },
                               "apiVersion": "v1alpha1",
                               "kind": "Setting",
                               "metadata": {}
                             }
                            """,
                        JsonUtils.objectToJson(newSetting),
                        true);

                    JSONAssert.assertEquals("""
                            {
                                "spec": {
                                    "displayName": "Fake Theme",
                                    "version": "*",
                                    "require": "*"
                                },
                                "apiVersion": "theme.halo.run/v1alpha1",
                                "kind": "Theme",
                                "metadata": {
                                    "name": "fake-theme"
                                }
                            }
                            """,
                        JsonUtils.objectToJson(newTheme),
                        true);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });
    }
}