package run.halo.app.core.extension.theme;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.theme.TemplateEngineManager;

/**
 * Tests for {@link ThemeEndpoint}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class ThemeEndpointTest {

    @Mock
    ThemeRootGetter themeRoot;

    @Mock
    ThemeService themeService;

    @Mock
    TemplateEngineManager templateEngineManager;

    @Mock
    private ReactiveExtensionClient client;

    @Mock
    private SystemConfigurableEnvironmentFetcher environmentFetcher;

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

            when(templateEngineManager.clearCache(eq("default")))
                .thenReturn(Mono.empty());

            webTestClient.post()
                .uri("/themes/default/upgrade")
                .body(fromMultipartData(bodyBuilder.build()))
                .exchange()
                .expectStatus().isOk();

            verify(themeService).upgrade(eq("default"), isA(InputStream.class));

            verify(templateEngineManager, times(1)).clearCache(eq("default"));
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
    void reloadTheme() {
        when(themeService.reloadTheme(any())).thenReturn(Mono.empty());
        webTestClient.put()
            .uri("/themes/fake/reload")
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    void resetSettingConfig() {
        when(themeService.resetSettingConfig(any())).thenReturn(Mono.empty());
        webTestClient.put()
            .uri("/themes/fake/reset-config")
            .exchange()
            .expectStatus().isOk();
    }

    @Nested
    class UpdateThemeConfigTest {

        @Test
        void updateWhenConfigMapNameIsNull() {
            Theme theme = new Theme();
            theme.setMetadata(new Metadata());
            theme.setSpec(new Theme.ThemeSpec());
            theme.getSpec().setConfigMapName(null);

            when(client.fetch(eq(Theme.class), eq("fake-theme"))).thenReturn(Mono.just(theme));
            webTestClient.put()
                .uri("/themes/fake-theme/config")
                .exchange()
                .expectStatus().isBadRequest();
        }

        @Test
        void updateWhenConfigMapNameNotMatch() {
            Theme theme = new Theme();
            theme.setMetadata(new Metadata());
            theme.setSpec(new Theme.ThemeSpec());
            theme.getSpec().setConfigMapName("fake-config-map");

            when(client.fetch(eq(Theme.class), eq("fake-theme"))).thenReturn(Mono.just(theme));
            webTestClient.put()
                .uri("/themes/fake-theme/config")
                .body(Mono.fromSupplier(() -> {
                    ConfigMap configMap = new ConfigMap();
                    configMap.setMetadata(new Metadata());
                    configMap.getMetadata().setName("not-match");
                    return configMap;
                }), ConfigMap.class)
                .exchange()
                .expectStatus().isBadRequest();
        }

        @Test
        void updateWhenConfigMapNameMatch() {
            Theme theme = new Theme();
            theme.setMetadata(new Metadata());
            theme.setSpec(new Theme.ThemeSpec());
            theme.getSpec().setConfigMapName("fake-config-map");

            when(client.fetch(eq(Theme.class), eq("fake-theme"))).thenReturn(Mono.just(theme));
            when(client.fetch(eq(ConfigMap.class), eq("fake-config-map"))).thenReturn(Mono.empty());
            when(client.create(any(ConfigMap.class))).thenReturn(Mono.empty());

            webTestClient.put()
                .uri("/themes/fake-theme/config")
                .body(Mono.fromSupplier(() -> {
                    ConfigMap configMap = new ConfigMap();
                    configMap.setMetadata(new Metadata());
                    configMap.getMetadata().setName("fake-config-map");
                    return configMap;
                }), ConfigMap.class)
                .exchange()
                .expectStatus().isOk();
        }
    }


    @Test
    void fetchActivatedTheme() {
        when(environmentFetcher.fetch(eq(SystemSetting.Theme.GROUP), eq(SystemSetting.Theme.class)))
            .thenReturn(Mono.fromSupplier(() -> {
                SystemSetting.Theme theme = new SystemSetting.Theme();
                theme.setActive("fake-activated");
                return theme;
            }));

        when(client.fetch(eq(Theme.class), eq("fake-activated"))).thenReturn(Mono.empty());
        webTestClient.get()
            .uri("/themes/-/activation")
            .exchange()
            .expectStatus().isOk();

        verify(client).fetch(eq(Theme.class), eq("fake-activated"));
    }

    @Test
    void fetchThemeSetting() {
        Theme theme = new Theme();
        theme.setMetadata(new Metadata());
        theme.getMetadata().setName("fake");
        theme.setSpec(new Theme.ThemeSpec());
        theme.getSpec().setSettingName("fake-setting");

        when(client.fetch(eq(Setting.class), eq("fake-setting")))
            .thenReturn(Mono.just(new Setting()));

        when(client.fetch(eq(Theme.class), eq("fake"))).thenReturn(Mono.just(theme));
        webTestClient.get()
            .uri("/themes/fake/setting")
            .exchange()
            .expectStatus().isOk();

        verify(client).fetch(eq(Setting.class), eq("fake-setting"));
        verify(client).fetch(eq(Theme.class), eq("fake"));
    }

    @Test
    void fetchThemeConfig() {
        Theme theme = new Theme();
        theme.setMetadata(new Metadata());
        theme.getMetadata().setName("fake");
        theme.setSpec(new Theme.ThemeSpec());
        theme.getSpec().setConfigMapName("fake-config");

        when(client.fetch(eq(ConfigMap.class), eq("fake-config")))
            .thenReturn(Mono.just(new ConfigMap()));

        when(client.fetch(eq(Theme.class), eq("fake"))).thenReturn(Mono.just(theme));
        webTestClient.get()
            .uri("/themes/fake/config")
            .exchange()
            .expectStatus().isOk();

        verify(client).fetch(eq(ConfigMap.class), eq("fake-config"));
        verify(client).fetch(eq(Theme.class), eq("fake"));
    }
}