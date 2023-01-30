package run.halo.app.core.extension.theme;

import static java.nio.file.Files.createTempDirectory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.infra.utils.FileUtils.deleteRecursivelyAndSilently;
import static run.halo.app.infra.utils.FileUtils.zip;

import com.github.zafarkhaja.semver.Version;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.util.ResourceUtils;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.AnnotationSetting;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Unstructured;
import run.halo.app.extension.exception.ExtensionException;
import run.halo.app.infra.SystemVersionSupplier;
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.infra.exception.ThemeInstallationException;
import run.halo.app.infra.utils.JsonUtils;

@ExtendWith(MockitoExtension.class)
class ThemeServiceImplTest {

    @Mock
    ReactiveExtensionClient client;

    @Mock
    ThemeRootGetter themeRoot;

    @Mock
    private SystemVersionSupplier systemVersionSupplier;

    @InjectMocks
    ThemeServiceImpl themeService;

    Path tmpDir;

    @BeforeEach
    void setUp() throws IOException {
        tmpDir = createTempDirectory("halo-theme-service-test-");
        lenient().when(themeRoot.get()).thenReturn(tmpDir.resolve("themes"));
        // init the folder
        Files.createDirectory(themeRoot.get());

        lenient().when(systemVersionSupplier.get()).thenReturn(Version.valueOf("0.0.0"));
    }

    @AfterEach
    void cleanUp() {
        deleteRecursivelyAndSilently(tmpDir);
    }

    Path prepareTheme(String themeFilename) throws IOException, URISyntaxException {
        var defaultThemeUri = ResourceUtils.getURL("classpath:themes/" + themeFilename).toURI();
        var defaultThemeZipPath = tmpDir.resolve("default.zip");
        zip(Path.of(defaultThemeUri), defaultThemeZipPath);
        return defaultThemeZipPath;
    }

    Theme createTheme() {
        return createTheme(theme -> {
        });
    }

    Theme createTheme(Consumer<Theme> customizer) {
        var metadata = new Metadata();
        metadata.setName("default");

        var spec = new Theme.ThemeSpec();
        spec.setDisplayName("Default");

        var theme = new Theme();
        theme.setMetadata(metadata);
        theme.setSpec(spec);
        customizer.accept(theme);
        return theme;
    }

    Unstructured convert(Theme theme) {
        return Unstructured.OBJECT_MAPPER.convertValue(theme, Unstructured.class);
    }

    @Nested
    class UpgradeTest {

        @Test
        void shouldFailIfThemeNotInstalledBefore() throws IOException, URISyntaxException {
            var themeZipPath = prepareTheme("other");
            when(client.fetch(Theme.class, "default")).thenReturn(Mono.empty());
            try (var is = Files.newInputStream(themeZipPath)) {
                StepVerifier.create(themeService.upgrade("default", is))
                    .verifyError(ServerWebInputException.class);
            }

            verify(client).fetch(Theme.class, "default");
        }

        @Test
        void shouldUpgradeSuccessfully() throws IOException, URISyntaxException {
            var themeZipPath = prepareTheme("other");

            var oldTheme = createTheme();
            when(client.fetch(Theme.class, "default"))
                // for old theme check
                .thenReturn(Mono.just(oldTheme))
                // for theme deletion
                .thenReturn(Mono.just(oldTheme))
                // for theme deleted check
                .thenReturn(Mono.empty());

            when(client.delete(oldTheme)).thenReturn(Mono.just(oldTheme));
            when(client.create(isA(Unstructured.class))).thenReturn(
                Mono.just(convert(createTheme(t -> t.getSpec().setDisplayName("New fake theme")))));

            try (var is = Files.newInputStream(themeZipPath)) {
                StepVerifier.create(themeService.upgrade("default", is))
                    .consumeNextWith(newTheme -> {
                        assertEquals("default", newTheme.getMetadata().getName());
                        assertEquals("New fake theme", newTheme.getSpec().getDisplayName());
                    })
                    .verifyComplete();
            }

            verify(client, times(3)).fetch(Theme.class, "default");
            verify(client).delete(oldTheme);
            verify(client).create(isA(Unstructured.class));
        }
    }

    @Nested
    class InstallTest {


        @Test
        void shouldInstallSuccessfully() throws IOException, URISyntaxException {
            var defaultThemeZipPath = prepareTheme("default");
            when(client.create(isA(Unstructured.class))).thenReturn(
                Mono.just(convert(createTheme())));
            try (var is = Files.newInputStream(defaultThemeZipPath)) {
                StepVerifier.create(themeService.install(is))
                    .consumeNextWith(theme -> {
                        assertEquals("default", theme.getMetadata().getName());
                        assertEquals("Default", theme.getSpec().getDisplayName());
                    })
                    .verifyComplete();
            }
        }

        @Test
        void shouldFailWhenPersistentError() throws IOException, URISyntaxException {
            var defaultThemeZipPath = prepareTheme("default");
            when(client.create(isA(Unstructured.class))).thenReturn(
                Mono.error(() -> new ExtensionException("Failed to create the extension")));
            try (var is = Files.newInputStream(defaultThemeZipPath)) {
                StepVerifier.create(themeService.install(is))
                    .verifyError(ExtensionException.class);
            }
        }

        @Test
        void shouldFailWhenThemeManifestIsInvalid() throws IOException, URISyntaxException {
            var defaultThemeZipPath = prepareTheme("invalid-missing-manifest");
            try (var is = Files.newInputStream(defaultThemeZipPath)) {
                StepVerifier.create(themeService.install(is))
                    .verifyError(ThemeInstallationException.class);
            }
        }
    }

    @Test
    void reloadThemeWhenSettingNameSetBeforeThenDeleteSetting() throws IOException {
        Theme theme = new Theme();
        theme.setMetadata(new Metadata());
        theme.getMetadata().setName("fake-theme");
        theme.setSpec(new Theme.ThemeSpec());
        theme.getSpec().setDisplayName("Hello");
        theme.getSpec().setSettingName("fake-setting");
        when(client.fetch(Theme.class, "fake-theme"))
            .thenReturn(Mono.just(theme));
        when(client.delete(any(Setting.class))).thenReturn(Mono.empty());
        Setting setting = new Setting();
        setting.setMetadata(new Metadata());
        setting.setSpec(new Setting.SettingSpec());
        setting.getSpec().setForms(List.of());
        when(client.fetch(Setting.class, "fake-setting"))
            .thenReturn(Mono.just(setting));

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
        when(client.update(any(Theme.class)))
            .thenAnswer((Answer<Mono<Theme>>) invocation -> {
                Theme argument = invocation.getArgument(0);
                return Mono.just(argument);
            });

        when(client.list(eq(AnnotationSetting.class), any(), eq(null))).thenReturn(Flux.empty());

        themeService.reloadTheme("fake-theme")
            .as(StepVerifier::create)
            .consumeNextWith(themeUpdated -> {
                try {
                    JSONAssert.assertEquals("""
                            {
                                "spec": {
                                    "displayName": "Fake Theme",
                                    "version": "*",
                                    "requires": "*"
                                },
                                "apiVersion": "theme.halo.run/v1alpha1",
                                "kind": "Theme",
                                "metadata": {
                                    "name": "fake-theme"
                                }
                            }
                            """,
                        JsonUtils.objectToJson(themeUpdated),
                        true);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            })
            .verifyComplete();
        // delete fake-setting
        verify(client, times(1)).delete(any(Setting.class));
        // Will not be created
        verify(client, times(0)).create(any(Setting.class));
    }

    @Test
    void reloadThemeWhenSettingNameNotSetBefore() throws IOException {
        Theme theme = new Theme();
        theme.setMetadata(new Metadata());
        theme.getMetadata().setName("fake-theme");
        theme.setSpec(new Theme.ThemeSpec());
        theme.getSpec().setDisplayName("Hello");
        when(client.fetch(Theme.class, "fake-theme"))
            .thenReturn(Mono.just(theme));
        Setting setting = new Setting();
        setting.setMetadata(new Metadata());
        setting.setSpec(new Setting.SettingSpec());
        setting.getSpec().setForms(List.of());

        when(client.fetch(eq(Setting.class), eq(null))).thenReturn(Mono.empty());

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
              settingName: fake-setting
            """);
        when(client.update(any(Theme.class)))
            .thenAnswer((Answer<Mono<Theme>>) invocation -> {
                Theme argument = invocation.getArgument(0);
                return Mono.just(argument);
            });

        when(client.create(any(Unstructured.class)))
            .thenAnswer((Answer<Mono<Unstructured>>) invocation -> {
                Unstructured argument = invocation.getArgument(0);
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
                           "metadata": {
                              "name": "fake-setting",
                              "labels": {
                                  "theme.halo.run/theme-name": "fake-theme"
                              }
                            }
                         }
                        """,
                    JsonUtils.objectToJson(argument),
                    true);
                return Mono.just(invocation.getArgument(0));
            });

        when(client.list(eq(AnnotationSetting.class), any(), eq(null))).thenReturn(Flux.empty());

        themeService.reloadTheme("fake-theme")
            .as(StepVerifier::create)
            .consumeNextWith(themeUpdated -> {
                try {
                    JSONAssert.assertEquals("""
                            {
                                "spec": {
                                    "settingName": "fake-setting",
                                    "displayName": "Fake Theme",
                                    "version": "*",
                                    "requires": "*"
                                },
                                "apiVersion": "theme.halo.run/v1alpha1",
                                "kind": "Theme",
                                "metadata": {
                                    "name": "fake-theme"
                                }
                            }
                            """,
                        JsonUtils.objectToJson(themeUpdated),
                        true);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            })
            .verifyComplete();
    }

    @Test
    void resetSettingConfig() {
        Theme theme = new Theme();
        theme.setMetadata(new Metadata());
        theme.getMetadata().setName("fake-theme");
        theme.setSpec(new Theme.ThemeSpec());
        theme.getSpec().setSettingName("fake-setting");
        theme.getSpec().setConfigMapName("fake-config");
        theme.getSpec().setDisplayName("Hello");
        when(client.fetch(Theme.class, "fake-theme"))
            .thenReturn(Mono.just(theme));

        Setting setting = new Setting();
        setting.setMetadata(new Metadata());
        setting.getMetadata().setName("fake-setting");
        setting.setSpec(new Setting.SettingSpec());
        var formSchemaItem = Map.of("name", "email", "value", "example@exmple.com");
        Setting.SettingForm settingForm = new Setting.SettingForm();
        settingForm.setGroup("basic");
        settingForm.setFormSchema(List.of(formSchemaItem));
        setting.getSpec().setForms(List.of(settingForm));
        when(client.fetch(eq(Setting.class), eq("fake-setting")))
            .thenReturn(Mono.just(setting));

        ConfigMap configMap = new ConfigMap();
        configMap.setMetadata(new Metadata());
        configMap.getMetadata().setName("fake-config");
        when(client.fetch(eq(ConfigMap.class), eq("fake-config")))
            .thenReturn(Mono.just(configMap));

        when(client.update(any(ConfigMap.class)))
            .thenAnswer((Answer<Mono<ConfigMap>>) invocation -> {
                ConfigMap argument = invocation.getArgument(0);
                JSONAssert.assertEquals("""
                        {
                            "data": {
                                "basic": "{\\"email\\":\\"example@exmple.com\\"}"
                            },
                            "apiVersion": "v1alpha1",
                            "kind": "ConfigMap",
                            "metadata": {
                                "name": "fake-config"
                            }
                        }
                        """,
                    JsonUtils.objectToJson(argument),
                    true);
                return Mono.just(invocation.getArgument(0));
            });

        themeService.resetSettingConfig("fake-theme")
            .as(StepVerifier::create)
            .consumeNextWith(next -> {
                assertThat(next).isNotNull();
            })
            .verifyComplete();

        verify(client, times(1))
            .fetch(eq(Setting.class), eq(setting.getMetadata().getName()));

        verify(client, times(1)).fetch(eq(ConfigMap.class), eq("fake-config"));

        verify(client, times(1)).update(any(ConfigMap.class));
    }
}