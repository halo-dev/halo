package run.halo.app.core.reconciler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.plugin.PluginConst.PLUGIN_PATH;
import static run.halo.app.plugin.PluginConst.RELOAD_ANNO;
import static run.halo.app.plugin.PluginConst.RUNTIME_MODE_ANNO;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pf4j.DefaultPluginDescriptor;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;
import org.springframework.core.io.DefaultResourceLoader;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.Plugin;
import run.halo.app.core.extension.ReverseProxy;
import run.halo.app.core.extension.Setting;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.extension.controller.RequeueException;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.plugin.PluginProperties;
import run.halo.app.plugin.PluginService;
import run.halo.app.plugin.SpringPluginManager;

/**
 * Tests for {@link PluginReconciler}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class PluginReconcilerTest {

    @Mock
    SpringPluginManager pluginManager;

    @Mock
    ExtensionClient client;

    @Mock
    PluginProperties pluginProperties;

    @Mock
    PluginService pluginService;

    @InjectMocks
    PluginReconciler reconciler;

    Clock clock = Clock.fixed(Instant.parse("2024-01-09T12:00:00Z"), ZoneOffset.UTC);

    String finalizer = "plugin-protection";
    String name = "fake-plugin";

    String reverseProxyName = "fake-plugin-system-generated-reverse-proxy";

    String settingName = "fake-setting";

    String configMapName = "fake-configmap";

    @BeforeEach
    void setUp() {
        reconciler.setClock(clock);
        reconciler.setScheduler(Schedulers.immediate());
    }

    @Test
    void shouldNotRequeueIfPluginNotFound() {
        when(client.fetch(Plugin.class, "fake-plugin")).thenReturn(Optional.empty());
        var result = reconciler.reconcile(new Request("fake-plugin"));
        assertFalse(result.reEnqueue());
        verify(client).fetch(Plugin.class, "fake-plugin");
    }

    @Nested
    class WhenNotDeleting {

        @TempDir
        Path tempPath;

        @BeforeEach
        void setUp() throws IOException {
            lenient().when(pluginService.getRequiredDependencies(any(), any())).thenReturn(List.of());
            Files.createFile(tempPath.resolve("fake-plugin-1.2.3.jar"));
        }

        @Test
        void shouldNotStartPluginWithDevModeInNonDevEnv() {
            var fakePlugin = createPlugin(name, plugin -> {
                var spec = plugin.getSpec();
                spec.setVersion("1.2.3");
                spec.setLogo("fake-logo.svg");
                spec.setEnabled(true);
                plugin.getMetadata()
                        .setAnnotations(new HashMap<>(Map.of(RUNTIME_MODE_ANNO, "dev", PLUGIN_PATH, "fake-path")));
            });

            when(client.fetch(Plugin.class, name)).thenReturn(Optional.of(fakePlugin));

            var result = reconciler.reconcile(new Request(name));
            assertFalse(result.reEnqueue());

            var status = fakePlugin.getStatus();
            assertEquals(Plugin.Phase.UNKNOWN, status.getPhase());
            var condition = status.getConditions().peekFirst();
            assertEquals(
                    Condition.builder()
                            .type(PluginReconciler.ConditionType.INITIALIZED)
                            .status(ConditionStatus.FALSE)
                            .reason(PluginReconciler.ConditionReason.INVALID_RUNTIME_MODE)
                            .message("""
                    Cannot run the plugin with development mode in non-development environment.\
                    """)
                            .build(),
                    condition);

            verify(client).update(fakePlugin);
            verify(client).fetch(Plugin.class, name);
            verify(pluginProperties).getRuntimeMode();
            verify(pluginManager, never()).loadPlugin(any(Path.class));
            verify(pluginManager, never()).startPlugin(name);
        }

        @Test
        void shouldStartInDevMode() {
            var fakePlugin = createPlugin(name, plugin -> {
                var spec = plugin.getSpec();
                spec.setVersion("1.2.3");
                spec.setLogo("fake-logo.svg");
                spec.setEnabled(true);
                plugin.getMetadata()
                        .setAnnotations(new HashMap<>(Map.of(RUNTIME_MODE_ANNO, "dev", PLUGIN_PATH, "fake-path")));
            });

            when(client.fetch(Plugin.class, name)).thenReturn(Optional.of(fakePlugin));
            when(pluginManager.getPlugin(name)).thenReturn(null).thenReturn(mockPluginWrapper(PluginState.RESOLVED));

            when(pluginManager.startPlugin(name)).thenReturn(PluginState.STARTED);
            when(pluginProperties.getRuntimeMode()).thenReturn(RuntimeMode.DEVELOPMENT);

            var result = reconciler.reconcile(new Request(name));
            assertTrue(result.reEnqueue());
            assertEquals(Paths.get("fake-path").toUri(), fakePlugin.getStatus().getLoadLocation());

            verify(pluginManager).startPlugin(name);
        }

        @Test
        void shouldThrowExceptionIfNoPluginPathProvidedInDevMode() {
            var fakePlugin = createPlugin(name, plugin -> {
                var spec = plugin.getSpec();
                spec.setVersion("1.2.3");
                spec.setLogo("fake-logo.svg");
                spec.setEnabled(true);
                plugin.getMetadata().setAnnotations(new HashMap<>(Map.of(RUNTIME_MODE_ANNO, "dev")));
            });

            when(client.fetch(Plugin.class, name)).thenReturn(Optional.of(fakePlugin));
            when(pluginManager.getPlugin(name))
                    // loading plugin
                    .thenReturn(null);
            when(pluginProperties.getRuntimeMode()).thenReturn(RuntimeMode.DEVELOPMENT);

            var result = reconciler.reconcile(new Request(name));
            assertFalse(result.reEnqueue());
        }

        @Test
        void shouldReloadIfReloadAnnotationPresent() throws IOException {
            var oldPluginFile = Files.createFile(tempPath.resolve("fake-plugin-1.0.0.jar"));
            var newPluginFile = tempPath.resolve("fake-plugin-1.2.3.jar");
            var fakePlugin = createPlugin(name, plugin -> {
                var spec = plugin.getSpec();
                spec.setVersion("1.2.3");
                spec.setLogo("fake-logo.svg");
                spec.setEnabled(true);
                plugin.getStatus().setLoadLocation(oldPluginFile.toUri());
                plugin.getMetadata()
                        .setAnnotations(new HashMap<>(
                                Map.of(RELOAD_ANNO, newPluginFile.toUri().toString())));
            });

            when(client.fetch(Plugin.class, name)).thenReturn(Optional.of(fakePlugin));
            when(pluginManager.getPluginsRoots()).thenReturn(List.of(tempPath));
            var pluginWrapper = mockPluginWrapper(PluginState.RESOLVED);
            when(pluginWrapper.getPluginPath()).thenReturn(oldPluginFile);
            when(pluginManager.getPlugin(name)).thenReturn(pluginWrapper);
            when(pluginManager.startPlugin(name)).thenReturn(PluginState.STARTED);
            when(pluginManager.getUnresolvedPlugins()).thenReturn(List.of(pluginWrapper));
            when(pluginManager.getResolvedPlugins()).thenReturn(List.of());
            when(pluginManager.unloadPlugin(name)).thenReturn(true);

            var result = reconciler.reconcile(new Request(name));
            assertTrue(result.reEnqueue());

            verify(pluginManager).unloadPlugin(name);
            var loadLocation = Paths.get(fakePlugin.getStatus().getLoadLocation());
            verify(pluginManager).loadPlugin(loadLocation);
            assertEquals(newPluginFile, loadLocation);
            assertFalse(Files.exists(oldPluginFile));
            assertFalse(fakePlugin.getMetadata().getAnnotations().containsKey(RELOAD_ANNO));
        }

        @Test
        void shouldKeepReloadRequestWhenUnloadFails() {
            var fakePlugin = createPlugin(name, plugin -> {
                var spec = plugin.getSpec();
                spec.setVersion("1.2.3");
                spec.setEnabled(true);
                plugin.getMetadata().setAnnotations(new HashMap<>(Map.of(RELOAD_ANNO, "true")));
            });

            when(client.fetch(Plugin.class, name)).thenReturn(Optional.of(fakePlugin));
            when(pluginManager.getPluginsRoots()).thenReturn(List.of(tempPath));
            var pluginWrapper = mockPluginWrapper(PluginState.RESOLVED);
            when(pluginManager.getPlugin(name)).thenReturn(pluginWrapper);
            when(pluginManager.getUnresolvedPlugins()).thenReturn(List.of(pluginWrapper));
            when(pluginManager.getResolvedPlugins()).thenReturn(List.of());
            when(pluginManager.unloadPlugin(name)).thenReturn(false);

            var result = reconciler.reconcile(new Request(name));

            assertTrue(result.reEnqueue());
            assertTrue(fakePlugin.getMetadata().getAnnotations().containsKey(RELOAD_ANNO));
            verify(pluginManager, never()).loadPlugin(any(Path.class));
        }

        @Test
        void shouldKeepPreviousLoadLocationAndReloadRequestWhenLoadingNewPluginFails() throws IOException {
            var oldPluginFile = Files.createFile(tempPath.resolve("fake-plugin-1.0.0.jar"));
            var newPluginFile = tempPath.resolve("fake-plugin-1.2.3.jar");
            var fakePlugin = createPlugin(name, plugin -> {
                var spec = plugin.getSpec();
                spec.setVersion("1.2.3");
                spec.setEnabled(true);
                plugin.getStatus().setLoadLocation(oldPluginFile.toUri());
                plugin.getMetadata()
                        .setAnnotations(new HashMap<>(
                                Map.of(RELOAD_ANNO, newPluginFile.toUri().toString())));
            });
            var pluginWrapper = mockPluginWrapper(PluginState.RESOLVED);
            when(pluginWrapper.getPluginPath()).thenReturn(oldPluginFile);

            when(client.fetch(Plugin.class, name)).thenReturn(Optional.of(fakePlugin));
            when(pluginManager.getPluginsRoots()).thenReturn(List.of(tempPath));
            when(pluginManager.getPlugin(name)).thenReturn(pluginWrapper);
            when(pluginManager.getUnresolvedPlugins()).thenReturn(List.of(pluginWrapper));
            when(pluginManager.getResolvedPlugins()).thenReturn(List.of());
            when(pluginManager.unloadPlugin(name)).thenReturn(true);
            doThrow(new IllegalStateException("Failed to load replacement"))
                    .when(pluginManager)
                    .loadPlugin(newPluginFile);

            assertThrows(IllegalStateException.class, () -> reconciler.reconcile(new Request(name)));

            assertEquals(oldPluginFile.toUri(), fakePlugin.getStatus().getLoadLocation());
            assertTrue(fakePlugin.getMetadata().getAnnotations().containsKey(RELOAD_ANNO));
        }

        @Test
        void shouldReloadWhenRuntimePathDiffersFromDesiredPath() throws IOException {
            var oldPluginFile = Files.createFile(tempPath.resolve("fake-plugin-1.0.0.jar"));
            var newPluginFile = tempPath.resolve("fake-plugin-1.2.3.jar");
            var fakePlugin = createPlugin(name, plugin -> {
                plugin.getSpec().setVersion("1.2.3");
                plugin.getSpec().setEnabled(false);
                plugin.getStatus().setLoadLocation(newPluginFile.toUri());
            });
            var pluginWrapper = mockPluginWrapper(PluginState.DISABLED);
            when(pluginWrapper.getPluginPath()).thenReturn(oldPluginFile);
            when(pluginWrapper.getPluginClassLoader()).thenReturn(mock(ClassLoader.class));

            when(client.fetch(Plugin.class, name)).thenReturn(Optional.of(fakePlugin));
            when(pluginManager.getPluginsRoots()).thenReturn(List.of(tempPath));
            when(pluginManager.getPlugin(name)).thenReturn(pluginWrapper);
            when(pluginManager.getUnresolvedPlugins()).thenReturn(List.of());
            when(pluginManager.getResolvedPlugins()).thenReturn(List.of(pluginWrapper));
            when(pluginManager.unloadPlugin(name)).thenReturn(true);

            reconciler.reconcile(new Request(name));

            verify(pluginManager).unloadPlugin(name);
            verify(pluginManager).loadPlugin(newPluginFile);
            assertFalse(Files.exists(oldPluginFile));
        }

        @Test
        void shouldReportIfFailedToStartPlugin() throws IOException {
            var fakePlugin = createPlugin(name, plugin -> {
                var spec = plugin.getSpec();
                spec.setVersion("1.2.3");
                spec.setLogo("fake-logo.svg");
                spec.setEnabled(true);
                spec.setSettingName(settingName);
                spec.setConfigMapName(configMapName);
            });
            fakePlugin.getStatus().setEntry("/plugins/fake-plugin/assets/ui/main.js?version=1.2.3");
            fakePlugin.getStatus().setStylesheet("/plugins/fake-plugin/assets/ui/style.css?version=1.2.3");

            when(client.fetch(Plugin.class, name)).thenReturn(Optional.of(fakePlugin));
            when(pluginManager.getPluginsRoots()).thenReturn(List.of(tempPath));
            when(pluginManager.getPlugin(name))
                    // loading plugin
                    .thenReturn(null)
                    // get setting extension
                    .thenReturn(mockPluginWrapperForSetting())
                    .thenReturn(mockPluginWrapperForStaticResources())
                    .thenReturn(mockPluginWrapper(PluginState.FAILED, new IllegalStateException("Fake error")));
            var result = reconciler.reconcile(new Request(name));
            assertFalse(result.reEnqueue());

            verify(client).update(fakePlugin);
            var status = fakePlugin.getStatus();
            assertEquals(Plugin.Phase.FAILED, status.getPhase());
            var condition = status.getConditions().peekFirst();
            assertEquals(PluginReconciler.ConditionType.READY, condition.getType());
            assertEquals(ConditionStatus.FALSE, condition.getStatus());
            assertEquals(PluginReconciler.ConditionReason.START_ERROR, condition.getReason());
            assertTrue(condition.getMessage().contains("Fake error"));

            verify(pluginManager, never()).startPlugin(name);
        }

        @Test
        void shouldEnablePluginIfEnabled() throws IOException {
            var fakePlugin = createPlugin(name, plugin -> {
                var spec = plugin.getSpec();
                spec.setVersion("1.2.3");
                spec.setLogo("fake-logo.svg");
                spec.setEnabled(true);
                spec.setSettingName(settingName);
                spec.setConfigMapName(configMapName);
            });

            when(client.fetch(Plugin.class, name)).thenReturn(Optional.of(fakePlugin));
            when(pluginManager.getPluginsRoots()).thenReturn(List.of(tempPath));
            when(pluginManager.getPlugin(name))
                    // loading plugin
                    .thenReturn(null)
                    // get setting extension
                    .thenReturn(mockPluginWrapperForSetting())
                    .thenReturn(mockPluginWrapperForStaticResources("ui"))
                    // before starting
                    .thenReturn(mockPluginWrapper(PluginState.STARTED))
                    // sync plugin state
                    .thenReturn(mockPluginWrapper(PluginState.STARTED));

            var result = reconciler.reconcile(new Request(name));

            assertFalse(result.reEnqueue());
            assertTrue(fakePlugin.getMetadata().getFinalizers().contains(finalizer));

            assertEquals(
                    "fake-plugin-1.2.3.jar",
                    fakePlugin.getMetadata().getAnnotations().get(PLUGIN_PATH));
            var loadLocation = Paths.get(fakePlugin.getStatus().getLoadLocation());
            assertEquals(tempPath.resolve("fake-plugin-1.2.3.jar"), loadLocation);
            assertEquals(
                    "/plugins/fake-plugin/assets/fake-logo.svg?version=1.2.3",
                    fakePlugin.getStatus().getLogo());
            assertEquals(
                    "/plugins/fake-plugin/assets/ui/main.js?version=1.2.3",
                    fakePlugin.getStatus().getEntry());
            assertEquals(
                    "/plugins/fake-plugin/assets/ui/style.css?version=1.2.3",
                    fakePlugin.getStatus().getStylesheet());
            assertEquals(Plugin.Phase.STARTED, fakePlugin.getStatus().getPhase());
            assertEquals(PluginState.STARTED, fakePlugin.getStatus().getLastProbeState());
            assertNotNull(fakePlugin.getStatus().getLastStartTime());

            var condition = fakePlugin.getStatus().getConditions().peek();
            assertEquals(PluginReconciler.ConditionType.READY, condition.getType());
            assertEquals(ConditionStatus.TRUE, condition.getStatus());
            assertEquals(clock.instant(), condition.getLastTransitionTime());

            verify(pluginManager, never()).startPlugin(name);
            verify(pluginManager).loadPlugin(loadLocation);
            verify(pluginManager, times(5)).getPlugin(name);
            verify(client).update(fakePlugin);
            verify(client).fetch(Setting.class, settingName);
            verify(client).create(any(Setting.class));
            verify(client).fetch(ConfigMap.class, configMapName);
            verify(client).create(any(ConfigMap.class));
            verify(client).fetch(ReverseProxy.class, reverseProxyName);
            verify(client).create(any(ReverseProxy.class));
        }

        @Test
        void shouldDisablePluginIfDisabled() throws IOException {
            var fakePlugin = createPlugin(name, plugin -> {
                var spec = plugin.getSpec();
                spec.setVersion("1.2.3");
                spec.setLogo("fake-logo.svg");
                spec.setEnabled(false);
                spec.setSettingName(settingName);
                spec.setConfigMapName(configMapName);
            });

            when(client.fetch(Plugin.class, name)).thenReturn(Optional.of(fakePlugin));
            when(pluginManager.getPluginsRoots()).thenReturn(List.of(tempPath));

            when(pluginManager.getPlugin(name))
                    // loading plugin
                    .thenReturn(null)
                    // get setting files.
                    .thenReturn(mockPluginWrapperForSetting())
                    // resolving static resources
                    .thenReturn(mockPluginWrapperForStaticResources())
                    // before disabling plugin
                    .thenReturn(mock(PluginWrapper.class))
                    // sync plugin state
                    .thenReturn(mockPluginWrapper(PluginState.DISABLED));

            var result = reconciler.reconcile(new Request("fake-plugin"));

            assertFalse(result.reEnqueue());
            assertTrue(fakePlugin.getMetadata().getFinalizers().contains(finalizer));

            assertEquals(
                    "fake-plugin-1.2.3.jar",
                    fakePlugin.getMetadata().getAnnotations().get(PLUGIN_PATH));
            var loadLocation = Paths.get(fakePlugin.getStatus().getLoadLocation());
            assertEquals(tempPath.resolve("fake-plugin-1.2.3.jar"), loadLocation);
            assertEquals(
                    "/plugins/fake-plugin/assets/fake-logo.svg?version=1.2.3",
                    fakePlugin.getStatus().getLogo());
            assertEquals(
                    "/plugins/fake-plugin/assets/console/main.js?version=1.2.3",
                    fakePlugin.getStatus().getEntry());
            assertEquals(
                    "/plugins/fake-plugin/assets/console/style.css?version=1.2.3",
                    fakePlugin.getStatus().getStylesheet());
            assertEquals(Plugin.Phase.DISABLED, fakePlugin.getStatus().getPhase());
            assertEquals(PluginState.DISABLED, fakePlugin.getStatus().getLastProbeState());

            verify(pluginManager).disablePlugin(name);
            verify(pluginManager).loadPlugin(loadLocation);
            verify(pluginManager, times(5)).getPlugin(name);
            verify(client).update(fakePlugin);
            verify(client).fetch(Setting.class, settingName);
            verify(client).create(any(Setting.class));
            verify(client).fetch(ConfigMap.class, configMapName);
            verify(client).create(any(ConfigMap.class));
            verify(client).fetch(ReverseProxy.class, reverseProxyName);
            verify(client).create(any(ReverseProxy.class));
        }

        @Test
        void shouldNotSetEntryOrStylesheetWhenNoStaticResources() throws IOException {
            var fakePlugin = createPlugin(name, plugin -> {
                var spec = plugin.getSpec();
                spec.setVersion("1.2.3");
                spec.setLogo("fake-logo.svg");
                spec.setEnabled(true);
                spec.setSettingName(settingName);
                spec.setConfigMapName(configMapName);
            });

            when(client.fetch(Plugin.class, name)).thenReturn(Optional.of(fakePlugin));
            when(pluginManager.getPluginsRoots()).thenReturn(List.of(tempPath));
            when(pluginManager.getPlugin(name))
                    // loading plugin
                    .thenReturn(null)
                    // get setting extension
                    .thenReturn(mockPluginWrapperForSetting())
                    // resolving static resources (no bundles at all)
                    .thenReturn(mockPluginWrapperForNoStaticResources())
                    // before starting
                    .thenReturn(mockPluginWrapper(PluginState.STARTED))
                    // sync plugin state
                    .thenReturn(mockPluginWrapper(PluginState.STARTED));

            var result = reconciler.reconcile(new Request(name));

            assertFalse(result.reEnqueue());
            assertNull(fakePlugin.getStatus().getEntry());
            assertNull(fakePlugin.getStatus().getStylesheet());
            assertEquals(Plugin.Phase.STARTED, fakePlugin.getStatus().getPhase());

            verify(pluginManager, times(5)).getPlugin(name);
            verify(client).update(fakePlugin);
        }

        PluginWrapper mockPluginWrapperForSetting() throws IOException {
            var pluginWrapper = mock(PluginWrapper.class);

            var pluginRootResource = new DefaultResourceLoader().getResource("classpath:plugin/plugin-0.0.1/");
            var classLoader = new URLClassLoader(new URL[] {pluginRootResource.getURL()}, null);
            when(pluginWrapper.getPluginClassLoader()).thenReturn(classLoader);
            lenient().when(pluginWrapper.getDescriptor()).thenReturn(new DefaultPluginDescriptor());
            return pluginWrapper;
        }

        PluginWrapper mockPluginWrapperForStaticResources() {
            return mockPluginWrapperForStaticResources("console");
        }

        PluginWrapper mockPluginWrapperForStaticResources(String location) {
            // check
            var pluginWrapper = mock(PluginWrapper.class);
            var pluginClassLoader = mock(ClassLoader.class);
            lenient().when(pluginClassLoader.getResource(location + "/main.js")).thenReturn(mock(URL.class));
            lenient()
                    .when(pluginClassLoader.getResource(location + "/style.css"))
                    .thenReturn(mock(URL.class));
            when(pluginWrapper.getPluginClassLoader()).thenReturn(pluginClassLoader);
            lenient().when(pluginWrapper.getDescriptor()).thenReturn(new DefaultPluginDescriptor());
            return pluginWrapper;
        }

        PluginWrapper mockPluginWrapperForNoStaticResources() {
            var pluginWrapper = mock(PluginWrapper.class);
            var pluginClassLoader = mock(ClassLoader.class);
            when(pluginWrapper.getPluginClassLoader()).thenReturn(pluginClassLoader);
            lenient().when(pluginWrapper.getDescriptor()).thenReturn(new DefaultPluginDescriptor());
            return pluginWrapper;
        }

        PluginWrapper mockPluginWrapper(PluginState state) {
            return mockPluginWrapper(state, null);
        }

        PluginWrapper mockPluginWrapper(PluginState state, @Nullable Throwable t) {
            var pluginWrapper = mock(PluginWrapper.class);
            lenient().when(pluginWrapper.getPluginState()).thenReturn(state);
            lenient().when(pluginWrapper.getDescriptor()).thenReturn(new DefaultPluginDescriptor());
            lenient().when(pluginWrapper.getFailedException()).thenReturn(t);
            return pluginWrapper;
        }
    }

    @Nested
    class WhenDeleting {

        @TempDir
        Path tempPath;

        @Test
        void shouldDoNothingWithoutFinalizer() {
            var fakePlugin = createPlugin(name, plugin -> {
                var metadata = plugin.getMetadata();
                metadata.setDeletionTimestamp(clock.instant());
            });

            when(client.fetch(Plugin.class, name)).thenReturn(Optional.of(fakePlugin));

            var result = reconciler.reconcile(new Request(name));
            assertFalse(result.reEnqueue());
            verify(client).fetch(Plugin.class, name);
            verify(client, never()).update(fakePlugin);
            verify(pluginManager, never()).getPlugin(name);
            verify(pluginManager, never()).deletePlugin(name);
        }

        @Test
        void shouldCleanUpResourceFully() {
            var fakePlugin = createPlugin(name, plugin -> {
                var metadata = plugin.getMetadata();
                metadata.setDeletionTimestamp(clock.instant());
                metadata.setFinalizers(new HashSet<>(Set.of(finalizer)));
                plugin.getStatus().setLastProbeState(PluginState.STARTED);
                plugin.getSpec().setConfigMapName("fake-configmap");
                plugin.getSpec().setSettingName("fake-setting");
            });

            when(client.fetch(Plugin.class, name)).thenReturn(Optional.of(fakePlugin));
            when(client.fetch(Setting.class, "fake-setting")).thenReturn(Optional.empty());
            when(client.fetch(ReverseProxy.class, reverseProxyName)).thenReturn(Optional.empty());

            when(pluginManager.getPlugin(name))
                    .thenReturn(mock(PluginWrapper.class))
                    .thenReturn(null);

            var result = reconciler.reconcile(new Request(name));

            assertFalse(result.reEnqueue());
            // Remove the finalizer after all plugin resources have been deleted.
            assertFalse(fakePlugin.getMetadata().getFinalizers().contains(finalizer));
            assertNull(fakePlugin.getStatus().getLastProbeState());
            verify(pluginManager, times(2)).getPlugin(name);
            verify(pluginManager).deletePlugin(name);
            verify(client).fetch(Plugin.class, name);
            verify(client).fetch(Setting.class, "fake-setting");
            verify(client).fetch(ReverseProxy.class, reverseProxyName);
            verify(client).update(fakePlugin);
        }

        @Test
        void shouldDeleteManagedPluginFileWhenPluginIsNotLoaded() throws IOException {
            var pluginFile = Files.createFile(tempPath.resolve("fake-plugin-1.2.3.jar"));
            var fakePlugin = createPlugin(name, plugin -> {
                var metadata = plugin.getMetadata();
                metadata.setDeletionTimestamp(clock.instant());
                metadata.setFinalizers(new HashSet<>(Set.of(finalizer)));
                plugin.getStatus().setLoadLocation(pluginFile.toUri());
            });

            when(client.fetch(Plugin.class, name)).thenReturn(Optional.of(fakePlugin));
            when(client.fetch(ReverseProxy.class, reverseProxyName)).thenReturn(Optional.empty());
            when(pluginManager.getPlugin(name)).thenReturn(null);
            when(pluginManager.getPluginsRoots()).thenReturn(List.of(tempPath));

            var result = reconciler.reconcile(new Request(name));

            assertFalse(result.reEnqueue());
            assertFalse(Files.exists(pluginFile));
            assertFalse(fakePlugin.getMetadata().getFinalizers().contains(finalizer));
            verify(client).update(fakePlugin);
        }

        @Test
        void shouldKeepFinalizerWhenPluginManagerStillContainsPlugin() {
            var fakePlugin = createPlugin(name, plugin -> {
                var metadata = plugin.getMetadata();
                metadata.setDeletionTimestamp(clock.instant());
                metadata.setFinalizers(new HashSet<>(Set.of(finalizer)));
            });
            var pluginWrapper = mock(PluginWrapper.class);

            when(client.fetch(Plugin.class, name)).thenReturn(Optional.of(fakePlugin));
            when(client.fetch(ReverseProxy.class, reverseProxyName)).thenReturn(Optional.empty());
            when(pluginManager.getPlugin(name)).thenReturn(pluginWrapper);
            when(pluginManager.deletePlugin(name)).thenReturn(false);

            var exception = assertThrows(RequeueException.class, () -> reconciler.reconcile(new Request(name)));

            assertEquals(Reconciler.Result.requeue(null), exception.getResult());
            assertTrue(fakePlugin.getMetadata().getFinalizers().contains(finalizer));
            verify(client, never()).update(fakePlugin);
        }

        @Test
        void shouldDeleteOnlyArtifactsOwnedByPlugin() throws Exception {
            var fakePluginResource = Paths.get(getClass()
                    .getClassLoader()
                    .getResource("plugin/plugin-0.0.2")
                    .toURI());
            var unrelatedPluginResource = Paths.get(getClass()
                    .getClassLoader()
                    .getResource("plugin/plugin-0.0.1")
                    .toURI());
            var currentPluginFile = tempPath.resolve("fake-plugin-1.2.3.jar");
            var stalePluginFile = tempPath.resolve("fake-plugin-1.0.0.jar");
            var similarlyNamedPluginFile = tempPath.resolve("fake-plugin-extra-1.0.0.jar");
            FileUtils.jar(fakePluginResource, currentPluginFile);
            FileUtils.jar(fakePluginResource, stalePluginFile);
            FileUtils.jar(unrelatedPluginResource, similarlyNamedPluginFile);

            var fakePlugin = createPlugin(name, plugin -> {
                var metadata = plugin.getMetadata();
                metadata.setDeletionTimestamp(clock.instant());
                metadata.setFinalizers(new HashSet<>(Set.of(finalizer)));
                plugin.getStatus().setLoadLocation(currentPluginFile.toUri());
            });

            when(client.fetch(Plugin.class, name)).thenReturn(Optional.of(fakePlugin));
            when(client.fetch(ReverseProxy.class, reverseProxyName)).thenReturn(Optional.empty());
            when(pluginManager.getPlugin(name)).thenReturn(null);
            when(pluginManager.getPluginsRoots()).thenReturn(List.of(tempPath));

            reconciler.reconcile(new Request(name));

            assertFalse(Files.exists(currentPluginFile));
            assertFalse(Files.exists(stalePluginFile));
            assertTrue(Files.exists(similarlyNamedPluginFile));
        }

        @Test
        void shouldDeleteSettingAndRequeueIfExists() {
            var fakePlugin = createPlugin(name, plugin -> {
                var metadata = plugin.getMetadata();
                metadata.setDeletionTimestamp(clock.instant());
                metadata.setFinalizers(new HashSet<>(Set.of(finalizer)));
                plugin.getStatus().setLastProbeState(PluginState.STARTED);
                plugin.getSpec().setSettingName(settingName);
            });

            var fakeSetting = createSetting(settingName);

            when(client.fetch(Plugin.class, name)).thenReturn(Optional.of(fakePlugin));
            when(client.fetch(Setting.class, settingName)).thenReturn(Optional.of(fakeSetting));
            when(client.fetch(ReverseProxy.class, reverseProxyName)).thenReturn(Optional.empty());

            var exception = assertThrows(RequeueException.class, () -> reconciler.reconcile(new Request(name)));
            assertEquals(Reconciler.Result.requeue(null), exception.getResult());
            assertEquals("Waiting for setting fake-setting to be deleted.", exception.getMessage());

            // Keep the finalizer until all plugin resources have been deleted.
            assertTrue(fakePlugin.getMetadata().getFinalizers().contains(finalizer));
            assertEquals(PluginState.STARTED, fakePlugin.getStatus().getLastProbeState());
            verify(pluginManager, never()).getPlugin(name);
            verify(pluginManager, never()).deletePlugin(name);
            verify(client).fetch(Plugin.class, name);
            verify(client).fetch(ReverseProxy.class, reverseProxyName);
            verify(client).fetch(Setting.class, settingName);
            verify(client).delete(fakeSetting);
            verify(client, never()).update(fakePlugin);
        }

        @Test
        void shouldDeleteReverseProxyAndRequeueIfExists() {
            var fakePlugin = createPlugin(name, plugin -> {
                var metadata = plugin.getMetadata();
                metadata.setDeletionTimestamp(clock.instant());
                metadata.setFinalizers(new HashSet<>(Set.of(finalizer)));
                plugin.getStatus().setLastProbeState(PluginState.STARTED);
                plugin.getSpec().setSettingName(settingName);
            });

            var reverseProxy = createReverseProxy(reverseProxyName);

            when(client.fetch(Plugin.class, name)).thenReturn(Optional.of(fakePlugin));
            when(client.fetch(ReverseProxy.class, reverseProxyName)).thenReturn(Optional.of(reverseProxy));

            var exception = assertThrows(
                    RequeueException.class,
                    () -> reconciler.reconcile(new Request(name)),
                    "Waiting for setting fake-setting to be deleted.");
            assertEquals(Reconciler.Result.requeue(null), exception.getResult());
            assertEquals("Waiting for reverse proxy " + reverseProxyName + " to be deleted.", exception.getMessage());

            // Keep the finalizer until all plugin resources have been deleted.
            assertTrue(fakePlugin.getMetadata().getFinalizers().contains(finalizer));
            assertEquals(PluginState.STARTED, fakePlugin.getStatus().getLastProbeState());
            verify(pluginManager, never()).getPlugin(name);
            verify(pluginManager, never()).deletePlugin(name);
            verify(client).fetch(Plugin.class, name);
            verify(client).fetch(ReverseProxy.class, reverseProxyName);
            verify(client).delete(reverseProxy);
            verify(client, never()).fetch(Setting.class, settingName);
            verify(client, never()).update(fakePlugin);
        }
    }

    Setting createSetting(String name) {
        var setting = new Setting();
        var metadata = new Metadata();
        metadata.setName(name);
        setting.setMetadata(metadata);
        return setting;
    }

    ReverseProxy createReverseProxy(String name) {
        var reverseProxy = new ReverseProxy();
        var metadata = new Metadata();
        metadata.setName(name);
        reverseProxy.setMetadata(metadata);
        return reverseProxy;
    }

    Plugin createPlugin(String name, Consumer<Plugin> pluginConsumer) {
        var plugin = new Plugin();
        var metadata = new Metadata();
        plugin.setMetadata(metadata);
        metadata.setName(name);
        plugin.setSpec(new Plugin.PluginSpec());
        plugin.setStatus(new Plugin.PluginStatus());
        pluginConsumer.accept(plugin);
        return plugin;
    }
}
