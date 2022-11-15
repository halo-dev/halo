package run.halo.app.core.extension.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link ThemeEndpoint}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class ThemeEndpointTest {

    @Mock
    ReactiveExtensionClient extensionClient;

    @Mock
    ThemeRootGetter themeRoot;

    @Mock
    ThemeService themeService;

    @InjectMocks
    ThemeEndpoint themeEndpoint;

    private Path tmpHaloWorkDir;

    WebTestClient webTestClient;

    private File defaultTheme;

    @BeforeEach
    void setUp() throws IOException {
        tmpHaloWorkDir = Files.createTempDirectory("halo-theme-endpoint-test");
        lenient().when(themeRoot.get()).thenReturn(tmpHaloWorkDir);
        defaultTheme = ResourceUtils.getFile("classpath:themes/test-theme.zip");
        webTestClient = WebTestClient
            .bindToRouterFunction(themeEndpoint.endpoint())
            .build();
    }

    @AfterEach
    void tearDown() throws IOException {
        FileSystemUtils.deleteRecursively(tmpHaloWorkDir);
    }

    @Nested
    class UpgradeTest {

        @Test
        void shouldNotOkIfThemeNotInstalled() {
            var bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("file", new FileSystemResource(defaultTheme))
                .contentType(MediaType.MULTIPART_FORM_DATA);

            when(themeService.upgrade(eq("invalid-missing-manifest"), isA(InputStream.class)))
                .thenReturn(
                    Mono.error(() -> new ServerWebInputException("Failed to upgrade theme")));

            webTestClient.post()
                .uri("/themes/invalid-missing-manifest/upgrade")
                .body(fromMultipartData(bodyBuilder.build()))
                .exchange()
                .expectStatus().isBadRequest();

            verify(themeService).upgrade(eq("invalid-missing-manifest"), isA(InputStream.class));
        }

        @Test
        void shouldUpgradeSuccessfullyIfThemeInstalled() {
            var bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("file", new FileSystemResource(defaultTheme))
                .contentType(MediaType.MULTIPART_FORM_DATA);

            var metadata = new Metadata();
            metadata.setName("default");
            var newTheme = new Theme();
            newTheme.setMetadata(metadata);

            when(themeService.upgrade(eq("default"), isA(InputStream.class)))
                .thenReturn(Mono.just(newTheme));

            webTestClient.post()
                .uri("/themes/default/upgrade")
                .body(fromMultipartData(bodyBuilder.build()))
                .exchange()
                .expectStatus().isOk();

            verify(themeService).upgrade(eq("default"), isA(InputStream.class));
        }

    }

    @Test
    void install() {
        var multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", new FileSystemResource(defaultTheme))
            .contentType(MediaType.MULTIPART_FORM_DATA);

        var installedTheme = new Theme();
        var metadata = new Metadata();
        metadata.setName("fake-name");
        installedTheme.setMetadata(metadata);
        when(themeService.install(any())).thenReturn(Mono.just(installedTheme));

        webTestClient.post()
            .uri("/themes/install")
            .body(fromMultipartData(multipartBodyBuilder.build()))
            .exchange()
            .expectStatus().isOk()
            .expectBody(Theme.class)
            .isEqualTo(installedTheme);

        verify(themeService).install(any());


        when(themeService.install(any())).thenReturn(
            Mono.error(new RuntimeException("Fake exception")));
        // Verify the theme is installed.
        webTestClient.post()
            .uri("/themes/install")
            .body(fromMultipartData(multipartBodyBuilder.build()))
            .exchange()
            .expectStatus().is5xxServerError();
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

        // when(haloProperties.getWorkDir()).thenReturn(tmpHaloWorkDir);
        Path themeWorkDir = themeRoot.get().resolve(theme.getMetadata().getName());
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