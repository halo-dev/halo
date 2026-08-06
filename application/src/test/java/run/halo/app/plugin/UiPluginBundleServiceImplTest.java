package run.halo.app.plugin;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginWrapper;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;
import reactor.core.publisher.Mono;
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

    UiPluginBundleServiceImpl service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new UiPluginBundleServiceImpl(pluginManager, themeService, themeRoot);
        service.setTempDir(tempDir.resolve("snapshots"));
        lenient().when(themeRoot.get()).thenReturn(tempDir.resolve("themes"));
        lenient().when(pluginManager.startedPlugins()).thenReturn(List.of());
        lenient().when(themeService.fetchActivatedTheme()).thenReturn(Mono.empty());
    }

    @Test
    void shouldBuildLegacySnapshotFromStartedPluginsAndActivatedTheme() throws Exception {
        var plugin = mockClasspathPlugin("legacy-plugin", "plugin/plugin-for-ui-assets");
        when(pluginManager.startedPlugins()).thenReturn(List.of(plugin));
        var activeTheme = prepareActiveTheme("active", "2.0.0");
        when(themeService.fetchActivatedTheme()).thenReturn(Mono.just(activeTheme));
        writeThemeUiFile("active", "main.js", readTestResource("theme/legacy-ui-assets/main.js"));
        writeThemeUiFile("active", "style.css", readTestResource("theme/legacy-ui-assets/style.css"));
        writeThemeUiFile("inactive", "main.js", "console.log('inactive-theme');");

        var snapshot = service.getProviderSnapshot().block();

        assertThat(snapshot).isNotNull();
        assertThat(snapshot.providers()).isEmpty();
        assertThat(snapshot.invalid()).isEmpty();
        assertThat(snapshot.registrations())
                .extracting(UiPluginProviderSnapshot.Registration::name)
                .containsExactly("legacy-plugin", "theme:active");
        assertThat(snapshot.legacy().script()).endsWith("/snapshots/" + snapshot.generation() + "/bundle.js");

        assertThat(read(service.getJsBundle(snapshot.generation()).block()))
                .contains("console.log(\"ui\");")
                .contains("VueUse.ref(\"legacy-plugin\")")
                .doesNotContain("console.log(\"console\");")
                .contains("VueUse.ref(\"legacy-theme\")")
                .doesNotContain("inactive-theme")
                .contains("{\"name\":\"legacy-plugin\",\"type\":\"plugin\",\"version\":\"1.0.0\"}")
                .contains(
                        "{\"name\":\"theme:active\",\"type\":\"theme\",\"themeName\":\"active\",\"version\":\"2.0.0\"}")
                .contains("this.enabledPlugins = [{\"name\":\"legacy-plugin\",\"version\":\"1.0.0\"}]");
        assertThat(read(service.getCssBundle(snapshot.generation()).block()))
                .contains(".ui")
                .contains(".legacy-theme");
    }

    @Test
    void shouldClassifyAndServeMixedEsmAndLegacyProviders() throws Exception {
        var esmPlugin = mockPlugin(
                "esm-plugin",
                Map.of(
                        "ui/ui-plugin.json",
                                "{\"format\":\"esm\",\"entry\":\"./main.js\",\"styles\":[\"./style.css\"]}",
                        "ui/main.js", "export { value } from './chunks/lazy.js';",
                        "ui/style.css", ".esm {}",
                        "ui/chunks/lazy.js", "export const value = 'lazy';"));
        var legacyPlugin = mockPlugin("legacy-plugin", Map.of("ui/main.js", "console.log('legacy');"));
        when(pluginManager.startedPlugins()).thenReturn(List.of(legacyPlugin, esmPlugin));

        var snapshot = service.getProviderSnapshot().block();

        assertThat(snapshot).isNotNull();
        assertThat(snapshot.registrations())
                .extracting(UiPluginProviderSnapshot.Registration::name)
                .containsExactly("esm-plugin", "legacy-plugin");
        assertThat(snapshot.providers()).singleElement().satisfies(provider -> {
            assertThat(provider.name()).isEqualTo("esm-plugin");
            assertThat(provider.entry()).endsWith("/providers/plugin/esm-plugin/main.js");
            assertThat(provider.styles()).singleElement().asString().endsWith("/providers/plugin/esm-plugin/style.css");
        });
        assertThat(read(service.getProviderResource(snapshot.generation(), "plugin", "esm-plugin", "chunks/lazy.js")
                        .block()))
                .isEqualTo("export const value = 'lazy';");
        assertThat(read(service.getJsBundle(snapshot.generation()).block()))
                .contains("console.log('legacy');")
                .doesNotContain("export const value")
                .doesNotContain("\"name\":\"esm-plugin\"")
                .contains("\"name\":\"legacy-plugin\"");
    }

    @Test
    void shouldRejectInvalidManifestsWithoutLegacyFallback() throws Exception {
        var extraField = mockPlugin(
                "extra-field",
                Map.of(
                        "ui/ui-plugin.json",
                                "{\"format\":\"esm\",\"entry\":\"./main.js\",\"styles\":[],\"targetHalo\":\"2.26.0\"}",
                        "ui/main.js", "console.log('must-not-run');"));
        var traversal = mockPlugin(
                "traversal",
                Map.of(
                        "ui/ui-plugin.json", "{\"format\":\"esm\",\"entry\":\"../outside.js\",\"styles\":[]}",
                        "outside.js", "console.log('outside');",
                        "ui/main.js", "console.log('must-not-fallback');"));
        when(pluginManager.startedPlugins()).thenReturn(List.of(traversal, extraField));

        var snapshot = service.getProviderSnapshot().block();

        assertThat(snapshot).isNotNull();
        assertThat(snapshot.providers()).isEmpty();
        assertThat(snapshot.invalid())
                .extracting(UiPluginProviderSnapshot.InvalidProvider::name)
                .containsExactly("extra-field", "traversal");
        assertThat(snapshot.invalid())
                .extracting(UiPluginProviderSnapshot.InvalidProvider::reason)
                .allMatch(reason -> reason.contains("Provider"));
        assertThat(service.getProviderResource(snapshot.generation(), "plugin", "extra-field", "main.js")
                        .block())
                .isNull();
        assertThat(read(service.getJsBundle(snapshot.generation()).block()))
                .doesNotContain("must-not-run", "must-not-fallback")
                .contains("this.enabledUiPlugins = [];this.enabledPlugins = []");
    }

    @Test
    void shouldRetainCurrentAndPreviousGenerationWithoutContentSubstitution() throws Exception {
        var source = tempDir.resolve("plugins/changing/ui/main.js");
        var plugin = mockPlugin("changing", Map.of("ui/main.js", "console.log('one');"));
        when(pluginManager.startedPlugins()).thenReturn(List.of(plugin));

        var first = service.getProviderSnapshot().block();
        Files.writeString(source, "console.log('two');");
        service.onPluginStarted(null);
        var second = service.getProviderSnapshot().block();

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(second.generation()).isNotEqualTo(first.generation());
        assertThat(read(service.getJsBundle(first.generation()).block()))
                .contains("one")
                .doesNotContain("two");
        assertThat(read(service.getJsBundle(second.generation()).block()))
                .contains("two")
                .doesNotContain("one");
        assertThat(service.snapshotCount()).isEqualTo(2);

        Files.writeString(source, "console.log('three');");
        service.onPluginStopped(null);
        var third = service.getProviderSnapshot().block();

        assertThat(third).isNotNull();
        assertThat(service.snapshotCount()).isEqualTo(2);
        assertThat(service.getJsBundle(first.generation()).block()).isNull();
        assertThat(read(service.getJsBundle(second.generation()).block())).contains("two");
        assertThat(read(service.getJsBundle(third.generation()).block())).contains("three");
    }

    @Test
    void shouldRebuildDevelopmentSnapshotWithoutLifecycleEvent() throws Exception {
        var source = tempDir.resolve("plugins/development/ui/main.js");
        var plugin = mockPlugin("development", Map.of("ui/main.js", "console.log('one');"));
        when(pluginManager.startedPlugins()).thenReturn(List.of(plugin));
        when(pluginManager.isDevelopment()).thenReturn(true);

        var first = service.getProviderSnapshot().block();
        Files.writeString(source, "console.log('two');");
        var second = service.getProviderSnapshot().block();

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(second.generation()).isNotEqualTo(first.generation());
    }

    @Test
    void shouldNotLoseInvalidationDuringSnapshotDiscovery() throws Exception {
        var discoveryStarted = new CountDownLatch(1);
        var continueDiscovery = new CountDownLatch(1);
        var subscriptions = new AtomicInteger();
        when(themeService.fetchActivatedTheme()).thenAnswer(invocation -> {
            if (subscriptions.incrementAndGet() == 1) {
                return Mono.defer(() -> {
                    discoveryStarted.countDown();
                    await(continueDiscovery);
                    return Mono.empty();
                });
            }
            return Mono.empty();
        });

        var first = CompletableFuture.supplyAsync(
                () -> service.getProviderSnapshot().block());
        assertThat(discoveryStarted.await(5, TimeUnit.SECONDS)).isTrue();
        service.onSystemConfigChanged(null);
        continueDiscovery.countDown();
        assertThat(first.get(5, TimeUnit.SECONDS)).isNotNull();

        assertThat(service.getProviderSnapshot().block()).isNotNull();
        assertThat(subscriptions).hasValue(2);
    }

    @Test
    void shouldExcludeInactiveThemeAndThemeWithoutLegacyScriptFromGlobals() throws Exception {
        var activeTheme = prepareActiveTheme("active", "1.0.0");
        when(themeService.fetchActivatedTheme()).thenReturn(Mono.just(activeTheme));
        writeThemeUiFile("active", "style.css", ".active {}");
        writeThemeUiFile("inactive", "main.js", "console.log('inactive');");

        var snapshot = service.getProviderSnapshot().block();

        assertThat(snapshot).isNotNull();
        assertThat(read(service.getJsBundle(snapshot.generation()).block())).doesNotContain("theme:active", "inactive");
        assertThat(read(service.getCssBundle(snapshot.generation()).block()))
                .contains(".active {}")
                .doesNotContain("inactive");
    }

    private PluginWrapper mockPlugin(String pluginId, Map<String, String> files) throws IOException {
        var pluginRoot = tempDir.resolve("plugins").resolve(pluginId);
        for (var file : files.entrySet()) {
            var path = pluginRoot.resolve(file.getKey());
            Files.createDirectories(path.getParent());
            Files.writeString(path, file.getValue());
        }
        var pluginWrapper = mock(PluginWrapper.class);
        var descriptor = mock(PluginDescriptor.class);
        var classLoader =
                new URLClassLoader(new java.net.URL[] {pluginRoot.toUri().toURL()});
        when(pluginWrapper.getPluginId()).thenReturn(pluginId);
        lenient().when(pluginWrapper.getPluginClassLoader()).thenReturn(classLoader);
        lenient().when(pluginWrapper.getDescriptor()).thenReturn(descriptor);
        lenient().when(descriptor.getVersion()).thenReturn("1.0.0");
        return pluginWrapper;
    }

    private PluginWrapper mockClasspathPlugin(String pluginId, String resourceRoot) throws IOException {
        var pluginRoot = ResourceUtils.getURL("classpath:" + resourceRoot + "/");
        var pluginWrapper = mock(PluginWrapper.class);
        var descriptor = mock(PluginDescriptor.class);
        var classLoader = new URLClassLoader(new java.net.URL[] {pluginRoot});
        when(pluginWrapper.getPluginId()).thenReturn(pluginId);
        lenient().when(pluginWrapper.getPluginClassLoader()).thenReturn(classLoader);
        lenient().when(pluginWrapper.getDescriptor()).thenReturn(descriptor);
        lenient().when(descriptor.getVersion()).thenReturn("1.0.0");
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
        var uiPath = themeRoot.get().resolve(themeName).resolve("ui-plugin").resolve("dist");
        Files.createDirectories(uiPath);
        Files.writeString(uiPath.resolve(filename), content);
    }

    private String readTestResource(String location) throws IOException {
        return Files.readString(ResourceUtils.getFile(ResourceUtils.getURL("classpath:" + location))
                .toPath());
    }

    private static String read(Resource resource) throws IOException {
        assertThat(resource).isNotNull();
        return resource.getContentAsString(UTF_8);
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for test latch");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
