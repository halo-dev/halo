package run.halo.app.plugin;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
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
import reactor.core.scheduler.Schedulers;
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
        service.setTempDir(tempDir.resolve("bundles"));
        lenient().when(themeRoot.get()).thenReturn(tempDir.resolve("themes"));
        lenient().when(pluginManager.startedPlugins()).thenReturn(List.of());
        lenient().when(themeService.fetchActivatedTheme()).thenReturn(Mono.empty());
    }

    @Test
    void shouldBuildVersionedLegacyDescriptorFromStartedPluginsAndActivatedTheme() throws Exception {
        var plugin = mockClasspathPlugin("legacy-plugin", "plugin/plugin-for-ui-assets");
        when(pluginManager.startedPlugins()).thenReturn(List.of(plugin));
        var activeTheme = prepareActiveTheme("active", "2.0.0");
        when(themeService.fetchActivatedTheme()).thenReturn(Mono.just(activeTheme));
        writeThemeUiFile("active", "main.js", readTestResource("theme/legacy-ui-assets/main.js"));
        writeThemeUiFile("active", "style.css", readTestResource("theme/legacy-ui-assets/style.css"));
        writeThemeUiFile("inactive", "main.js", "console.log('inactive-theme');");

        var descriptor = service.getProviderDescriptor().block();

        assertThat(descriptor).isNotNull();
        assertThat(descriptor.providers()).isEmpty();
        assertThat(descriptor.invalid()).isEmpty();
        assertThat(descriptor.registrations())
                .extracting(UiPluginProviderDescriptor.Registration::name)
                .containsExactly("legacy-plugin", "theme:active");
        assertThat(descriptor.legacy().script())
                .isEqualTo("/apis/api.console.halo.run/v1alpha1/ui-plugins/-/bundle.js?v=" + descriptor.version());
        assertThat(descriptor.styles())
                .extracting(UiPluginProviderDescriptor.Style::name, UiPluginProviderDescriptor.Style::type)
                .containsExactly(tuple("legacy-plugin", "plugin"), tuple("theme:active", "theme"));
        assertVersionedUrl(descriptor.styles().getFirst().href(), "/plugins/legacy-plugin/assets/ui/style.css");
        assertVersionedUrl(descriptor.styles().get(1).href(), "/themes/active/ui-plugin/assets/style.css");

        assertThat(read(service.getJsBundle(descriptor.version()).block()))
                .contains("console.log(\"ui\");")
                .contains("VueUse.ref(\"legacy-plugin\")")
                .doesNotContain("console.log(\"console\");")
                .contains("VueUse.ref(\"legacy-theme\")")
                .doesNotContain("inactive-theme")
                .contains("{\"name\":\"legacy-plugin\",\"type\":\"plugin\",\"version\":\"1.0.0\"}")
                .contains(
                        "{\"name\":\"theme:active\",\"type\":\"theme\",\"themeName\":\"active\",\"version\":\"2.0.0\"}")
                .contains("this.enabledPlugins = [{\"name\":\"legacy-plugin\",\"version\":\"1.0.0\"}]");
        assertThat(read(service.getCssBundle(descriptor.version()).block()))
                .isEqualTo(descriptor.styles().stream()
                        .map(style -> "@import url(\"" + style.href() + "\");\n")
                        .collect(java.util.stream.Collectors.joining()));
    }

    @Test
    void shouldDescribeEsmResourcesThroughExistingStaticMappings() throws Exception {
        var uiPlugin = mockPlugin(
                "ui-plugin",
                Map.of(
                        "ui/ui-plugin.json",
                                "{\"format\":\"esm\",\"entry\":\"./main.js\",\"style\":\"./styles/main.css\"}",
                        "ui/main.js", "export default {};",
                        "ui/styles/main.css", ".ui {}"));
        var consolePlugin = mockPlugin(
                "console-plugin",
                Map.of(
                        "console/ui-plugin.json", "{\"format\":\"esm\",\"entry\":\"./main.js\"}",
                        "console/main.js", "export default {};"));
        when(pluginManager.startedPlugins()).thenReturn(List.of(uiPlugin, consolePlugin));

        var activeTheme = prepareActiveTheme("active", "2.0.0");
        when(themeService.fetchActivatedTheme()).thenReturn(Mono.just(activeTheme));
        writeThemeUiFile(
                "active",
                "ui-plugin.json",
                "{\"format\":\"esm\",\"entry\":\"./chunks/main.js\",\"style\":\"./styles/theme.css\"}");
        writeThemeUiFile("active", "chunks/main.js", "export default {};");
        writeThemeUiFile("active", "styles/theme.css", ".theme {}");

        var descriptor = service.getProviderDescriptor().block();

        assertThat(descriptor).isNotNull();
        assertThat(descriptor.providers())
                .extracting(UiPluginProviderDescriptor.EsmProvider::name, UiPluginProviderDescriptor.EsmProvider::type)
                .containsExactly(
                        tuple("console-plugin", "plugin"),
                        tuple("ui-plugin", "plugin"),
                        tuple("theme:active", "theme"));
        assertVersionedUrl(descriptor.providers().getFirst().entry(), "/plugins/console-plugin/assets/console/main.js");
        assertVersionedUrl(descriptor.providers().get(1).entry(), "/plugins/ui-plugin/assets/ui/main.js");
        assertVersionedUrl(descriptor.providers().get(2).entry(), "/themes/active/ui-plugin/assets/chunks/main.js");
        assertThat(descriptor.styles())
                .extracting(UiPluginProviderDescriptor.Style::name, UiPluginProviderDescriptor.Style::type)
                .containsExactly(tuple("ui-plugin", "plugin"), tuple("theme:active", "theme"));
        assertVersionedUrl(descriptor.styles().getFirst().href(), "/plugins/ui-plugin/assets/ui/styles/main.css");
        assertVersionedUrl(descriptor.styles().get(1).href(), "/themes/active/ui-plugin/assets/styles/theme.css");
        assertThat(read(service.getCssBundle(descriptor.version()).block()))
                .isEqualTo(descriptor.styles().stream()
                        .map(style -> "@import url(\"" + style.href() + "\");\n")
                        .collect(java.util.stream.Collectors.joining()));
        assertThat(read(service.getJsBundle(descriptor.version()).block()))
                .doesNotContain("export default")
                .contains("this.enabledUiPlugins = [];this.enabledPlugins = []");
    }

    @Test
    void shouldRejectInvalidManifestsWithoutLegacyFallback() throws Exception {
        var extraField = mockPlugin(
                "extra-field",
                Map.of(
                        "ui/ui-plugin.json", "{\"format\":\"esm\",\"entry\":\"./main.js\",\"targetHalo\":\"2.26.0\"}",
                        "ui/main.js", "console.log('must-not-run');"));
        var traversal = mockPlugin(
                "traversal",
                Map.of(
                        "ui/ui-plugin.json", "{\"format\":\"esm\",\"entry\":\"../outside.js\"}",
                        "outside.js", "console.log('outside');",
                        "ui/main.js", "console.log('must-not-fallback');"));
        when(pluginManager.startedPlugins()).thenReturn(List.of(traversal, extraField));

        var descriptor = service.getProviderDescriptor().block();

        assertThat(descriptor).isNotNull();
        assertThat(descriptor.providers()).isEmpty();
        assertThat(descriptor.invalid())
                .extracting(UiPluginProviderDescriptor.InvalidProvider::name)
                .containsExactly("extra-field", "traversal");
        assertThat(read(service.getJsBundle(descriptor.version()).block()))
                .doesNotContain("must-not-run", "must-not-fallback")
                .contains("this.enabledUiPlugins = [];this.enabledPlugins = []");
    }

    @Test
    void shouldKeepDevelopmentDescriptorStableWhenProviderResourcesDoNotChange() throws Exception {
        var plugin = mockPlugin(
                "development",
                Map.of(
                        "ui/ui-plugin.json",
                        "{\"format\":\"esm\",\"entry\":\"./main.js\",\"style\":\"./style.css\"}",
                        "ui/main.js",
                        "export default {};",
                        "ui/style.css",
                        ".development {}"));
        when(pluginManager.startedPlugins()).thenReturn(List.of(plugin));
        when(pluginManager.isDevelopment()).thenReturn(true);

        var first = service.getProviderDescriptor().block();

        var second = service.getProviderDescriptor().block();

        assertThat(second).isEqualTo(first);
    }

    @Test
    void shouldChangeDevelopmentProviderUrlWhenEntryChanges() throws Exception {
        var plugin = mockPlugin(
                "development",
                Map.of(
                        "ui/ui-plugin.json", "{\"format\":\"esm\",\"entry\":\"./main.js\"}",
                        "ui/main.js", "export default {};"));
        when(pluginManager.startedPlugins()).thenReturn(List.of(plugin));
        when(pluginManager.isDevelopment()).thenReturn(true);

        var first = service.getProviderDescriptor().block();

        Files.writeString(tempDir.resolve("plugins/development/ui/main.js"), "export default { changed: true };");
        var second = service.getProviderDescriptor().block();

        assertThat(second.providers().getFirst().entry())
                .isNotEqualTo(first.providers().getFirst().entry());
        assertThat(second.version()).isNotEqualTo(first.version());
    }

    @Test
    void shouldKeepUnchangedDevelopmentProviderUrlWhenAnotherProviderChanges() throws Exception {
        var alpha = mockPlugin(
                "alpha",
                Map.of(
                        "ui/ui-plugin.json", "{\"format\":\"esm\",\"entry\":\"./main.js\"}",
                        "ui/main.js", "export default {};"));
        var beta = mockPlugin(
                "beta",
                Map.of(
                        "ui/ui-plugin.json", "{\"format\":\"esm\",\"entry\":\"./main.js\"}",
                        "ui/main.js", "export default {};"));
        when(pluginManager.startedPlugins()).thenReturn(List.of(alpha, beta));
        when(pluginManager.isDevelopment()).thenReturn(true);

        var first = service.getProviderDescriptor().block();

        Files.writeString(tempDir.resolve("plugins/alpha/ui/main.js"), "export default { changed: true };");
        var second = service.getProviderDescriptor().block();

        assertThat(second.providers().getFirst().entry())
                .isNotEqualTo(first.providers().getFirst().entry());
        assertThat(second.providers().get(1).entry())
                .isEqualTo(first.providers().get(1).entry());
    }

    @Test
    void shouldExcludeInactiveThemeAndThemeWithoutLegacyScriptFromGlobals() throws Exception {
        var activeTheme = prepareActiveTheme("active", "1.0.0");
        when(themeService.fetchActivatedTheme()).thenReturn(Mono.just(activeTheme));
        writeThemeUiFile("active", "style.css", ".active {}");
        writeThemeUiFile("inactive", "main.js", "console.log('inactive');");

        var descriptor = service.getProviderDescriptor().block();

        assertThat(descriptor).isNotNull();
        assertThat(read(service.getJsBundle(descriptor.version()).block())).doesNotContain("theme:active", "inactive");
        assertThat(read(service.getCssBundle(descriptor.version()).block()))
                .isEqualTo("@import url(\"" + descriptor.styles().getFirst().href() + "\");\n");
    }

    @Test
    void shouldClassifyThemeResourcesOnBoundedElastic() throws Exception {
        var activeTheme = prepareActiveTheme("active", "1.0.0");
        writeThemeUiFile("active", "ui-plugin.json", "{\"format\":\"esm\",\"entry\":\"./main.js\"}");
        writeThemeUiFile("active", "main.js", "export default {};");
        var lookupThread = new AtomicReference<String>();
        when(themeRoot.get()).thenAnswer(invocation -> {
            lookupThread.set(Thread.currentThread().getName());
            return tempDir.resolve("themes");
        });
        var themeScheduler = Schedulers.newSingle("theme-source");
        when(themeService.fetchActivatedTheme())
                .thenReturn(Mono.just(activeTheme).publishOn(themeScheduler));

        try {
            assertThat(service.getProviderDescriptor().block()).isNotNull();
            assertThat(lookupThread.get()).startsWith("boundedElastic-");
        } finally {
            themeScheduler.dispose();
        }
    }

    private PluginWrapper mockPlugin(String pluginId, Map<String, String> files) throws IOException {
        var pluginRoot = tempDir.resolve("plugins").resolve(pluginId);
        for (var file : files.entrySet()) {
            var path = pluginRoot.resolve(file.getKey());
            Files.createDirectories(path.getParent());
            Files.writeString(path, file.getValue());
        }
        return mockPlugin(
                pluginId,
                new URLClassLoader(new java.net.URL[] {pluginRoot.toUri().toURL()}));
    }

    private PluginWrapper mockClasspathPlugin(String pluginId, String resourceRoot) throws IOException {
        var pluginRoot = ResourceUtils.getURL("classpath:" + resourceRoot + "/");
        return mockPlugin(pluginId, new URLClassLoader(new java.net.URL[] {pluginRoot}));
    }

    private PluginWrapper mockPlugin(String pluginId, URLClassLoader classLoader) {
        var pluginWrapper = mock(PluginWrapper.class);
        var descriptor = mock(PluginDescriptor.class);
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
        var uiPath = themeRoot.get().resolve(themeName).resolve("ui-plugin").resolve("dist");
        var file = uiPath.resolve(filename);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }

    private String readTestResource(String location) throws IOException {
        return Files.readString(ResourceUtils.getFile(ResourceUtils.getURL("classpath:" + location))
                .toPath());
    }

    private static String read(Resource resource) throws IOException {
        assertThat(resource).isNotNull();
        return resource.getContentAsString(UTF_8);
    }

    private static void assertVersionedUrl(String actual, String path) {
        assertThat(actual).matches(java.util.regex.Pattern.quote(path) + "\\?v=[0-9a-f]{64}");
    }
}
