package run.halo.app.plugin;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.hash.Hashing;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginWrapper;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.util.ResourceUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.theme.service.ThemeService;

@ExtendWith(MockitoExtension.class)
class UiPluginBundleServiceImplTest {

    @Mock
    SpringPluginManager pluginManager;

    @Mock
    ThemeService themeService;

    @Mock
    ThemeRootGetter themeRoot;

    UiPluginBundleServiceImpl uiPluginBundleService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        uiPluginBundleService = new UiPluginBundleServiceImpl(pluginManager, themeService, themeRoot);
        uiPluginBundleService.setTempDir(tempDir.resolve("bundles"));
        lenient().when(themeRoot.get()).thenReturn(tempDir.resolve("themes"));
    }

    @Test
    void shouldAggregateStartedPluginsAndActivatedThemeJsBundles() throws IOException {
        var plugin = mockStartedPlugin("fake-plugin", "plugin-for-ui-assets");
        when(pluginManager.startedPlugins()).thenReturn(List.of(plugin));
        var activeTheme = prepareActiveTheme("active", "1.0.0");
        when(themeService.fetchActivatedTheme()).thenReturn(Mono.just(activeTheme));
        writeThemeUiFile("active", "main.js", "console.log(\"theme\");");
        writeThemeUiFile("inactive", "main.js", "console.log(\"inactive\");");

        toString(uiPluginBundleService.uglifyJsBundle())
                .as(StepVerifier::create)
                .assertNext(content -> assertThat(content)
                        .contains("// Generated from plugin fake-plugin")
                        .contains("console.log(\"ui\");")
                        .contains("// Generated from theme active")
                        .contains("console.log(\"theme\");")
                        .contains("""
                            this.enabledUiPlugins = [{"name":"fake-plugin","type":"plugin","version":"1.0.0"},{"name":"theme:active","type":"theme","themeName":"active","version":"1.0.0"}]\
                            """)
                        .doesNotContain("console.log(\"inactive\");"))
                .verifyComplete();
    }

    @Test
    void shouldAggregateStartedPluginsAndActivatedThemeCssBundles() throws IOException {
        var plugin = mockStartedPlugin("fake-plugin", "plugin-for-ui-assets");
        when(pluginManager.startedPlugins()).thenReturn(List.of(plugin));
        var activeTheme = prepareActiveTheme("active", "1.0.0");
        when(themeService.fetchActivatedTheme()).thenReturn(Mono.just(activeTheme));
        writeThemeUiFile("active", "style.css", ".theme {}");
        writeThemeUiFile("inactive", "style.css", ".inactive {}");

        toString(uiPluginBundleService.uglifyCssBundle())
                .as(StepVerifier::create)
                .assertNext(content -> assertThat(content)
                        .contains("/* Generated from plugin fake-plugin */")
                        .contains(".ui")
                        .contains("/* Generated from theme active */")
                        .contains(".theme {}")
                        .doesNotContain(".inactive {}"))
                .verifyComplete();
    }

    @Test
    void shouldReturnOnlyPluginMetadataWhenActivatedThemeHasNoJsBundle() {
        var plugin = mockStartedPlugin("fake-plugin", "plugin-for-ui-assets");
        when(pluginManager.startedPlugins()).thenReturn(List.of(plugin));
        var activeTheme = prepareActiveTheme("active", "1.0.0");
        when(themeService.fetchActivatedTheme()).thenReturn(Mono.just(activeTheme));

        toString(uiPluginBundleService.uglifyJsBundle())
                .as(StepVerifier::create)
                .assertNext(content -> assertThat(content).contains("""
                            this.enabledUiPlugins = [{"name":"fake-plugin","type":"plugin","version":"1.0.0"}]\
                            """).doesNotContain("theme:active"))
                .verifyComplete();
    }

    @Test
    void shouldGenerateVersionFromStartedPluginsAndActivatedTheme() {
        var plugin = mockStartedPlugin("fake-plugin", "plugin-for-ui-assets");
        when(pluginManager.startedPlugins()).thenReturn(List.of(plugin));
        var activeTheme = prepareActiveTheme("active", "1.0.0");
        when(themeService.fetchActivatedTheme()).thenReturn(Mono.just(activeTheme));

        var expected = Hashing.sha256()
                .hashUnencodedChars("fake-plugin:1.0.0theme:active:1.0.0")
                .toString();
        uiPluginBundleService
                .generateBundleVersion()
                .as(StepVerifier::create)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldGenerateRandomBundleVersionInDevelopment() {
        var clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        uiPluginBundleService.setClock(clock);
        when(pluginManager.isDevelopment()).thenReturn(true);

        uiPluginBundleService
                .generateBundleVersion()
                .as(StepVerifier::create)
                .expectNext(String.valueOf(clock.instant().toEpochMilli()))
                .verifyComplete();

        verify(pluginManager, never()).startedPlugins();
    }

    private PluginWrapper mockStartedPlugin(String pluginId, String resourceRoot) {
        var pluginWrapper = mock(PluginWrapper.class);
        var descriptor = mock(PluginDescriptor.class);
        var pluginRoot = resourceUrl("classpath:plugin/" + resourceRoot + "/");
        var classLoader = new URLClassLoader(new URL[] {pluginRoot});
        when(pluginWrapper.getPluginId()).thenReturn(pluginId);
        lenient().when(pluginWrapper.getPluginClassLoader()).thenReturn(classLoader);
        lenient().when(pluginWrapper.getDescriptor()).thenReturn(descriptor);
        lenient().when(descriptor.getVersion()).thenReturn("1.0.0");
        lenient().when(pluginManager.getPlugin(pluginId)).thenReturn(pluginWrapper);
        return pluginWrapper;
    }

    private Theme prepareActiveTheme(String name, String version) {
        var metadata = new Metadata();
        metadata.setName(name);
        var spec = new Theme.ThemeSpec();
        spec.setVersion(version);
        var theme = new Theme();
        theme.setMetadata(metadata);
        theme.setSpec(spec);
        return theme;
    }

    private void writeThemeUiFile(String themeName, String filename, String content) throws IOException {
        var uiPath = themeRoot.get().resolve(themeName).resolve("ui");
        Files.createDirectories(uiPath);
        Files.writeString(uiPath.resolve(filename), content);
    }

    private URL resourceUrl(String location) {
        try {
            return ResourceUtils.getURL(location);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Mono<String> toString(Flux<DataBuffer> dataBuffers) {
        return dataBuffers
                .map(dataBuffer -> {
                    var bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return new String(bytes, UTF_8);
                })
                .collect(Collectors.joining());
    }
}
