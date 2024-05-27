package run.halo.app.core.extension.reconciler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import run.halo.app.plugin.PluginProperties;
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

        @Test
        void shouldNotStartPluginWithDevModeInNonDevEnv() {
            var fakePlugin = createPlugin(name, plugin -> {
                var spec = plugin.getSpec();
                spec.setVersion("1.2.3");
                spec.setLogo("fake-logo.svg");
                spec.setEnabled(true);
                plugin.getMetadata()
                    .setAnnotations(new HashMap<>(Map.of(RUNTIME_MODE_ANNO, "dev",
                        PLUGIN_PATH, "fake-path")));
            });

            when(client.fetch(Plugin.class, name)).thenReturn(Optional.of(fakePlugin));

            var result = reconciler.reconcile(new Request(name));
            assertFalse(result.reEnqueue());

            var status = fakePlugin.getStatus();
            assertEquals(Plugin.Phase.UNKNOWN, status.getPhase());
            var condition = status.getConditions().peekFirst();
            assertEquals(Condition.builder()
                .type(PluginReconciler.ConditionType.INITIALIZED)
                .status(ConditionStatus.FALSE)
                .reason(PluginReconciler.ConditionReason.INVALID_RUNTIME_MODE)
                .message("""
                    Cannot run the plugin with development mode in non-development environment.\
                    """)
                .build(), condition);

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
                    .setAnnotations(new HashMap<>(Map.of(RUNTIME_MODE_ANNO, "dev",
                        PLUGIN_PATH, "fake-path")));
            });

            when(client.fetch(Plugin.class, name)).thenReturn(Optional.of(fakePlugin));
            when(pluginManager.getPlugin(name))
                .thenReturn(null)
                .thenReturn(mockPluginWrapper(PluginState.RESOLVED));

            when(pluginManager.startPlugin(name)).thenReturn(PluginState.STARTED);
            when(pluginProperties.getRuntimeMode()).thenReturn(RuntimeMode.DEVELOPMENT);

            var result = reconciler.reconcile(new Request(name));
            assertFalse(result.reEnqueue());
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
                plugin.getMetadata()
                    .setAnnotations(new HashMap<>(Map.of(RUNTIME_MODE_ANNO, "dev")));
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
        void shouldReloadIfReloadAnnotationPresent() {
            var fakePlugin = createPlugin(name, plugin -> {
                var spec = plugin.getSpec();
                spec.setVersion("1.2.3");
                spec.setLogo("fake-logo.svg");
                spec.setEnabled(true);
                plugin.getMetadata().setAnnotations(new HashMap<>(Map.of(RELOAD_ANNO, "true")));
            });

            when(client.fetch(Plugin.class, name)).thenReturn(Optional.of(fakePlugin));
            when(pluginManager.getPluginsRoots()).thenReturn(List.of(tempPath));
            var pluginWrapper = mockPluginWrapper(PluginState.RESOLVED);
            when(pluginManager.getPlugin(name)).thenReturn(pluginWrapper);
            when(pluginManager.startPlugin(name)).thenReturn(PluginState.STARTED);
            when(pluginManager.getUnresolvedPlugins()).thenReturn(List.of(pluginWrapper));
            when(pluginManager.getResolvedPlugins()).thenReturn(List.of());

            var result = reconciler.reconcile(new Request(name));
            assertFalse(result.reEnqueue());

            verify(pluginManager).unloadPlugin(name);
            var loadLocation = Paths.get(fakePlugin.getStatus().getLoadLocation());
            verify(pluginManager).loadPlugin(loadLocation);
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

            when(client.fetch(Plugin.class, name)).thenReturn(Optional.of(fakePlugin));
            when(pluginManager.getPluginsRoots()).thenReturn(List.of(tempPath));
            when(pluginManager.getPlugin(name))
                // loading plugin
                .thenReturn(null)
                // get setting extension
                .thenReturn(mockPluginWrapperForSetting())
                .thenReturn(mockPluginWrapperForStaticResources());
            when(pluginManager.startPlugin(name)).thenReturn(PluginState.FAILED);

            var result = reconciler.reconcile(new Request(name));
            assertFalse(result.reEnqueue());

            verify(client).update(fakePlugin);
            var status = fakePlugin.getStatus();
            assertEquals(Plugin.Phase.FAILED, status.getPhase());
            var condition = status.getConditions().peekFirst();
            assertEquals(Condition.builder()
                .type(PluginReconciler.ConditionType.READY)
                .status(ConditionStatus.FALSE)
                .reason(PluginReconciler.ConditionReason.START_ERROR)
                .message("Failed to start plugin fake-plugin(FAILED).")
                .build(), condition);
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
                .thenReturn(mockPluginWrapperForStaticResources())
                // before starting
                .thenReturn(mockPluginWrapper(PluginState.STOPPED))
                // sync plugin state
                .thenReturn(mockPluginWrapper(PluginState.STARTED));
            when(pluginManager.startPlugin(name)).thenReturn(PluginState.STARTED);

            var result = reconciler.reconcile(new Request(name));

            assertFalse(result.reEnqueue());
            assertTrue(fakePlugin.getMetadata().getFinalizers().contains(finalizer));

            assertEquals("fake-plugin-1.2.3.jar",
                fakePlugin.getMetadata().getAnnotations().get(PLUGIN_PATH));
            var loadLocation = Paths.get(fakePlugin.getStatus().getLoadLocation());
            assertEquals(tempPath.resolve("fake-plugin-1.2.3.jar"), loadLocation);
            assertEquals("/plugins/fake-plugin/assets/fake-logo.svg?version=1.2.3",
                fakePlugin.getStatus().getLogo());
            assertEquals("/plugins/fake-plugin/assets/console/main.js?version=1.2.3",
                fakePlugin.getStatus().getEntry());
            assertEquals("/plugins/fake-plugin/assets/console/style.css?version=1.2.3",
                fakePlugin.getStatus().getStylesheet());
            assertEquals(Plugin.Phase.STARTED, fakePlugin.getStatus().getPhase());
            assertEquals(PluginState.STARTED, fakePlugin.getStatus().getLastProbeState());
            assertNotNull(fakePlugin.getStatus().getLastStartTime());

            var condition = fakePlugin.getStatus().getConditions().peek();
            assertEquals(PluginReconciler.ConditionType.READY, condition.getType());
            assertEquals(ConditionStatus.TRUE, condition.getStatus());
            assertEquals(clock.instant(), condition.getLastTransitionTime());

            verify(pluginManager).startPlugin(name);
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

            assertEquals("fake-plugin-1.2.3.jar",
                fakePlugin.getMetadata().getAnnotations().get(PLUGIN_PATH));
            var loadLocation = Paths.get(fakePlugin.getStatus().getLoadLocation());
            assertEquals(tempPath.resolve("fake-plugin-1.2.3.jar"), loadLocation);
            assertEquals("/plugins/fake-plugin/assets/fake-logo.svg?version=1.2.3",
                fakePlugin.getStatus().getLogo());
            assertEquals("/plugins/fake-plugin/assets/console/main.js?version=1.2.3",
                fakePlugin.getStatus().getEntry());
            assertEquals("/plugins/fake-plugin/assets/console/style.css?version=1.2.3",
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

        PluginWrapper mockPluginWrapperForSetting() throws IOException {
            var pluginWrapper = mock(PluginWrapper.class);

            var pluginRootResource =
                new DefaultResourceLoader().getResource("classpath:plugin/plugin-0.0.1/");
            var classLoader = new URLClassLoader(new URL[] {pluginRootResource.getURL()}, null);
            when(pluginWrapper.getPluginClassLoader()).thenReturn(classLoader);
            lenient().when(pluginWrapper.getDescriptor()).thenReturn(new DefaultPluginDescriptor());
            return pluginWrapper;
        }

        PluginWrapper mockPluginWrapperForStaticResources() {
            // check
            var pluginWrapper = mock(PluginWrapper.class);
            var pluginClassLoader = mock(ClassLoader.class);
            when(pluginClassLoader.getResource("console/main.js")).thenReturn(
                mock(URL.class));
            when(pluginClassLoader.getResource("console/style.css")).thenReturn(
                mock(URL.class));
            when(pluginWrapper.getPluginClassLoader()).thenReturn(pluginClassLoader);
            lenient().when(pluginWrapper.getDescriptor()).thenReturn(new DefaultPluginDescriptor());
            return pluginWrapper;
        }

        PluginWrapper mockPluginWrapper(PluginState state) {
            var pluginWrapper = mock(PluginWrapper.class);
            lenient().when(pluginWrapper.getPluginState()).thenReturn(state);
            lenient().when(pluginWrapper.getDescriptor()).thenReturn(new DefaultPluginDescriptor());
            return pluginWrapper;
        }

    }

    @Nested
    class WhenDeleting {

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
            when(client.fetch(Setting.class, "fake-setting"))
                .thenReturn(Optional.empty());
            when(client.fetch(ReverseProxy.class, reverseProxyName))
                .thenReturn(Optional.empty());

            when(pluginManager.getPlugin(name))
                .thenReturn(mock(PluginWrapper.class))
                .thenReturn(null);

            var result = reconciler.reconcile(new Request(name));

            assertFalse(result.reEnqueue());
            // make sure the finalizer is removed.
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
            when(client.fetch(Setting.class, settingName))
                .thenReturn(Optional.of(fakeSetting));
            when(client.fetch(ReverseProxy.class, reverseProxyName))
                .thenReturn(Optional.empty());

            var exception = assertThrows(
                RequeueException.class,
                () -> reconciler.reconcile(new Request(name))
            );
            assertEquals(Reconciler.Result.requeue(null), exception.getResult());
            assertEquals("Waiting for setting fake-setting to be deleted.", exception.getMessage());

            // make sure the finalizer is removed.
            assertFalse(fakePlugin.getMetadata().getFinalizers().contains(finalizer));
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
            when(client.fetch(ReverseProxy.class, reverseProxyName))
                .thenReturn(Optional.of(reverseProxy));

            var exception = assertThrows(RequeueException.class,
                () -> reconciler.reconcile(new Request(name)),
                "Waiting for setting fake-setting to be deleted.");
            assertEquals(Reconciler.Result.requeue(null), exception.getResult());
            assertEquals("Waiting for reverse proxy " + reverseProxyName + " to be deleted.",
                exception.getMessage());

            // make sure the finalizer is removed.
            assertFalse(fakePlugin.getMetadata().getFinalizers().contains(finalizer));
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