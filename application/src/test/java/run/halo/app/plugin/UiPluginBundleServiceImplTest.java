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
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;
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
        var bundleVersion = generateBundleVersion();

        assertThat(descriptor).isNotNull();
        assertThat(descriptor.providers())
                .extracting(
                        UiPluginProviderDescriptor.Provider::name,
                        UiPluginProviderDescriptor.Provider::type,
                        UiPluginProviderDescriptor.Provider::kind)
                .containsExactly(tuple("legacy-plugin", "plugin", "legacy"), tuple("theme:active", "theme", "legacy"));
        assertThat(descriptor.legacyScript())
                .isEqualTo("/apis/api.console.halo.run/v1alpha1/ui-plugins/-/bundle.js?v=" + bundleVersion);
        assertVersionedUrl(descriptor.providers().getFirst().style(), "/plugins/legacy-plugin/assets/ui/style.css");
        assertVersionedUrl(descriptor.providers().get(1).style(), "/themes/active/ui-plugin/assets/style.css");

        assertThat(read(service.getJsBundle(bundleVersion).block()))
                .contains("console.log(\"ui\");")
                .contains("VueUse.ref(\"legacy-plugin\")")
                .doesNotContain("console.log(\"console\");")
                .contains("VueUse.ref(\"legacy-theme\")")
                .doesNotContain("inactive-theme")
                .contains("{\"name\":\"legacy-plugin\",\"type\":\"plugin\",\"version\":\"1.0.0\"}")
                .contains(
                        "{\"name\":\"theme:active\",\"type\":\"theme\",\"themeName\":\"active\",\"version\":\"2.0.0\"}")
                .contains("this.enabledPlugins = [{\"name\":\"legacy-plugin\",\"version\":\"1.0.0\"}]");
        assertThat(read(service.getCssBundle(bundleVersion).block()))
                .isEqualTo(descriptor.providers().stream()
                        .map(UiPluginProviderDescriptor.Provider::style)
                        .filter(Objects::nonNull)
                        .map(style -> "@import url(\"" + style + "\");\n")
                        .collect(java.util.stream.Collectors.joining()));
    }

    @Test
    void shouldDescribeEsmResourcesThroughExistingStaticMappings() throws Exception {
        var uiPlugin = mockPlugin(
                "ui-plugin",
                Map.of(
                        "ui/ui-plugin.json",
                                "{\"format\":\"esm\",\"entry\":\"./main.12345678.js\",\"style\":\"./styles/main.12345678.css\"}",
                        "ui/main.12345678.js", "export default {};",
                        "ui/styles/main.12345678.css", ".ui {}"));
        var consolePlugin = mockPlugin(
                "console-plugin",
                Map.of(
                        "console/ui-plugin.json", "{\"format\":\"esm\",\"entry\":\"./main.abcdef12.js\"}",
                        "console/main.abcdef12.js", "export default {};"));
        when(pluginManager.startedPlugins()).thenReturn(List.of(uiPlugin, consolePlugin));

        var activeTheme = prepareActiveTheme("active", "2.0.0");
        when(themeService.fetchActivatedTheme()).thenReturn(Mono.just(activeTheme));
        writeThemeUiFile(
                "active",
                "ui-plugin.json",
                "{\"format\":\"esm\",\"entry\":\"./chunks/main.87654321.js\",\"style\":\"./styles/theme.87654321.css\"}");
        writeThemeUiFile("active", "chunks/main.87654321.js", "export default {};");
        writeThemeUiFile("active", "styles/theme.87654321.css", ".theme {}");

        var descriptor = service.getProviderDescriptor().block();
        var bundleVersion = generateBundleVersion();

        assertThat(descriptor).isNotNull();
        assertThat(descriptor.providers())
                .extracting(
                        UiPluginProviderDescriptor.Provider::name,
                        UiPluginProviderDescriptor.Provider::type,
                        UiPluginProviderDescriptor.Provider::kind)
                .containsExactly(
                        tuple("console-plugin", "plugin", "esm"),
                        tuple("ui-plugin", "plugin", "esm"),
                        tuple("theme:active", "theme", "esm"));
        assertThat(descriptor.legacyScript()).isNull();
        assertThat(descriptor.providers().getFirst().entry())
                .isEqualTo("/plugins/console-plugin/assets/console/main.abcdef12.js");
        assertThat(descriptor.providers().get(1).entry()).isEqualTo("/plugins/ui-plugin/assets/ui/main.12345678.js");
        assertThat(descriptor.providers().get(2).entry())
                .isEqualTo("/themes/active/ui-plugin/assets/chunks/main.87654321.js");
        assertThat(descriptor.providers().getFirst().style()).isNull();
        assertThat(descriptor.providers().get(1).style())
                .isEqualTo("/plugins/ui-plugin/assets/ui/styles/main.12345678.css");
        assertThat(descriptor.providers().get(2).style())
                .isEqualTo("/themes/active/ui-plugin/assets/styles/theme.87654321.css");
        assertThat(read(service.getCssBundle(bundleVersion).block()))
                .isEqualTo(descriptor.providers().stream()
                        .map(UiPluginProviderDescriptor.Provider::style)
                        .filter(Objects::nonNull)
                        .map(style -> "@import url(\"" + style + "\");\n")
                        .collect(java.util.stream.Collectors.joining()));
        assertThat(read(service.getJsBundle(bundleVersion).block()))
                .doesNotContain("export default")
                .contains(
                        "this.enabledUiPlugins = [{\"name\":\"console-plugin\",\"type\":\"plugin\",\"version\":\"1.0.0\"},{\"name\":\"ui-plugin\",\"type\":\"plugin\",\"version\":\"1.0.0\"},{\"name\":\"theme:active\",\"type\":\"theme\",\"themeName\":\"active\",\"version\":\"2.0.0\"}]")
                .contains(
                        "this.enabledPlugins = [{\"name\":\"console-plugin\",\"version\":\"1.0.0\"},{\"name\":\"ui-plugin\",\"version\":\"1.0.0\"}]");
    }

    @Test
    void shouldExposeEsmProvidersThroughLegacyEnabledMetadata() throws Exception {
        var legacyPlugin = mockClasspathPlugin("legacy-plugin", "plugin/plugin-for-ui-assets");
        var esmPlugin = mockPlugin(
                "esm-plugin",
                Map.of(
                        "ui/ui-plugin.json", "{\"format\":\"esm\",\"entry\":\"./main.js\"}",
                        "ui/main.js", "export default {};"));
        when(pluginManager.startedPlugins()).thenReturn(List.of(legacyPlugin, esmPlugin));

        var bundleVersion = generateBundleVersion();
        var bundle = read(service.getJsBundle(bundleVersion).block());

        assertThat(bundle)
                .contains("console.log(\"ui\");")
                .doesNotContain("export default {};")
                .contains(
                        "this.enabledUiPlugins = [{\"name\":\"esm-plugin\",\"type\":\"plugin\",\"version\":\"1.0.0\"},{\"name\":\"legacy-plugin\",\"type\":\"plugin\",\"version\":\"1.0.0\"}]")
                .contains(
                        "this.enabledPlugins = [{\"name\":\"esm-plugin\",\"version\":\"1.0.0\"},{\"name\":\"legacy-plugin\",\"version\":\"1.0.0\"}]");
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
        var bundleVersion = generateBundleVersion();

        assertThat(descriptor).isNotNull();
        assertThat(descriptor.legacyScript()).isNull();
        assertThat(descriptor.providers())
                .extracting(UiPluginProviderDescriptor.Provider::name)
                .containsExactly("extra-field", "traversal");
        assertThat(descriptor.providers())
                .extracting(
                        UiPluginProviderDescriptor.Provider::kind,
                        UiPluginProviderDescriptor.Provider::entry,
                        UiPluginProviderDescriptor.Provider::style)
                .containsOnly(tuple("invalid", null, null));
        assertThat(read(service.getJsBundle(bundleVersion).block()))
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
                        "ui/ui-plugin.json", "{\"format\":\"esm\",\"entry\":\"./main.11111111.js\"}",
                        "ui/main.11111111.js", "export default {};"));
        when(pluginManager.startedPlugins()).thenReturn(List.of(plugin));
        when(pluginManager.isDevelopment()).thenReturn(true);

        var first = service.getProviderDescriptor().block();
        var firstVersion = generateBundleVersion();

        Files.writeString(
                tempDir.resolve("plugins/development/ui/main.22222222.js"), "export default { changed: true };");
        Files.writeString(
                tempDir.resolve("plugins/development/ui/ui-plugin.json"),
                "{\"format\":\"esm\",\"entry\":\"./main.22222222.js\"}");
        var second = service.getProviderDescriptor().block();
        var secondVersion = generateBundleVersion();

        assertThat(second.providers().getFirst().entry())
                .isNotEqualTo(first.providers().getFirst().entry());
        assertThat(secondVersion).isNotEqualTo(firstVersion);
    }

    @Test
    void shouldKeepUnchangedDevelopmentProviderUrlWhenAnotherProviderChanges() throws Exception {
        var alpha = mockPlugin(
                "alpha",
                Map.of(
                        "ui/ui-plugin.json", "{\"format\":\"esm\",\"entry\":\"./main.11111111.js\"}",
                        "ui/main.11111111.js", "export default {};"));
        var beta = mockPlugin(
                "beta",
                Map.of(
                        "ui/ui-plugin.json", "{\"format\":\"esm\",\"entry\":\"./main.11111111.js\"}",
                        "ui/main.11111111.js", "export default {};"));
        when(pluginManager.startedPlugins()).thenReturn(List.of(alpha, beta));
        when(pluginManager.isDevelopment()).thenReturn(true);

        var first = service.getProviderDescriptor().block();

        Files.writeString(tempDir.resolve("plugins/alpha/ui/main.22222222.js"), "export default { changed: true };");
        Files.writeString(
                tempDir.resolve("plugins/alpha/ui/ui-plugin.json"),
                "{\"format\":\"esm\",\"entry\":\"./main.22222222.js\"}");
        var second = service.getProviderDescriptor().block();

        assertThat(second.providers().getFirst().entry())
                .isNotEqualTo(first.providers().getFirst().entry());
        assertThat(second.providers().get(1).entry())
                .isEqualTo(first.providers().get(1).entry());
    }

    @Test
    void shouldExposeActiveCssOnlyThemeInLegacyGlobals() throws Exception {
        var activeTheme = prepareActiveTheme("active", "1.0.0");
        when(themeService.fetchActivatedTheme()).thenReturn(Mono.just(activeTheme));
        writeThemeUiFile("active", "style.css", ".active {}");
        writeThemeUiFile("inactive", "main.js", "console.log('inactive');");

        var descriptor = service.getProviderDescriptor().block();
        var bundleVersion = generateBundleVersion();

        assertThat(descriptor).isNotNull();
        assertThat(read(service.getJsBundle(bundleVersion).block()))
                .contains("\"name\":\"theme:active\"")
                .doesNotContain("inactive");
        assertThat(read(service.getCssBundle(bundleVersion).block()))
                .isEqualTo("@import url(\"" + descriptor.providers().getFirst().style() + "\");\n");
    }

    @Test
    void shouldRejectActivatedThemeWithUnsatisfiedHaloVersion() throws Exception {
        var activeTheme = prepareActiveTheme("active", "1.0.0");
        var status = new Theme.ThemeStatus();
        status.setPhase(Theme.ThemePhase.FAILED);
        activeTheme.setStatus(status);
        Theme.nullSafeConditionList(activeTheme)
                .addAndEvictFIFO(Condition.builder()
                        .type(Theme.ThemePhase.FAILED.name())
                        .status(ConditionStatus.FALSE)
                        .reason("UnsatisfiedRequiresVersion")
                        .message("Theme requires Halo >=3.0.0.")
                        .lastTransitionTime(Instant.now())
                        .build());
        when(themeService.fetchActivatedTheme()).thenReturn(Mono.just(activeTheme));
        writeThemeUiFile("active", "ui-plugin.json", "{\"format\":\"esm\",\"entry\":\"./main.js\"}");
        writeThemeUiFile("active", "main.js", "export default {};");

        var descriptor = service.getProviderDescriptor().block();

        assertThat(descriptor).isNotNull();
        assertThat(descriptor.legacyScript()).isNull();
        assertThat(descriptor.providers())
                .extracting(
                        UiPluginProviderDescriptor.Provider::kind,
                        UiPluginProviderDescriptor.Provider::entry,
                        UiPluginProviderDescriptor.Provider::reason)
                .containsExactly(tuple("invalid", null, "Theme requires Halo >=3.0.0."));
    }

    @Test
    void shouldIgnoreHistoricalUnsatisfiedRequiresCondition() throws Exception {
        var activeTheme = prepareActiveTheme("active", "1.0.0");
        var status = new Theme.ThemeStatus();
        status.setPhase(Theme.ThemePhase.FAILED);
        activeTheme.setStatus(status);
        var conditions = Theme.nullSafeConditionList(activeTheme);
        conditions.addAndEvictFIFO(Condition.builder()
                .type("Compatibility")
                .status(ConditionStatus.FALSE)
                .reason("UnsatisfiedRequiresVersion")
                .message("Historical compatibility failure.")
                .lastTransitionTime(Instant.now())
                .build());
        conditions.addAndEvictFIFO(Condition.builder()
                .type(Theme.ThemePhase.FAILED.name())
                .status(ConditionStatus.FALSE)
                .reason("ThemeConfigError")
                .message("Current unrelated failure.")
                .lastTransitionTime(Instant.now())
                .build());
        when(themeService.fetchActivatedTheme()).thenReturn(Mono.just(activeTheme));
        writeThemeUiFile("active", "main.js", "console.log('active');");

        var descriptor = service.getProviderDescriptor().block();

        assertThat(descriptor).isNotNull();
        assertThat(descriptor.providers())
                .extracting(UiPluginProviderDescriptor.Provider::kind)
                .containsExactly("legacy");
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

    private String generateBundleVersion() {
        return Objects.requireNonNull(service.generateBundleVersion().block());
    }

    private static void assertVersionedUrl(String actual, String path) {
        assertThat(actual).matches(java.util.regex.Pattern.quote(path) + "\\?v=[0-9a-f]{64}");
    }
}
