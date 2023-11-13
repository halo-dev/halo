package run.halo.app.core.extension.reconciler;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.core.extension.reconciler.PluginReconciler.initialReverseProxyName;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.context.ApplicationEventPublisher;
import run.halo.app.core.extension.Plugin;
import run.halo.app.core.extension.ReverseProxy;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.plugin.HaloPluginManager;
import run.halo.app.plugin.HaloPluginWrapper;
import run.halo.app.plugin.PluginConst;
import run.halo.app.plugin.PluginStartingError;

/**
 * Tests for {@link PluginReconciler}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class PluginReconcilerTest {

    @Mock
    HaloPluginManager haloPluginManager;

    @Mock
    ExtensionClient extensionClient;

    @Mock
    HaloPluginWrapper pluginWrapper;

    @Mock
    ApplicationEventPublisher eventPublisher;

    PluginReconciler pluginReconciler;

    @BeforeEach
    void setUp() {
        pluginReconciler = new PluginReconciler(extensionClient, haloPluginManager, eventPublisher);
        lenient().when(haloPluginManager.getPluginsRoot()).thenReturn(Paths.get("plugins"));
        lenient().when(haloPluginManager.validatePluginVersion(any())).thenReturn(true);
        lenient().when(haloPluginManager.getSystemVersion()).thenReturn("0.0.0");
        lenient().when(haloPluginManager.getPlugin(any())).thenReturn(pluginWrapper);
        lenient().when(haloPluginManager.getUnresolvedPlugins()).thenReturn(List.of());
    }

    @Test
    @DisplayName("Reconcile to start successfully")
    void reconcileOkWhenPluginManagerStartSuccessfully() {
        Plugin plugin = need2ReconcileForStartupState();
        when(pluginWrapper.getPluginState()).thenReturn(PluginState.STOPPED);
        var pluginDescriptor = mock(PluginDescriptor.class);
        when(pluginWrapper.getDescriptor()).thenReturn(pluginDescriptor);
        when(pluginDescriptor.getVersion()).thenReturn("1.0.0");

        when(extensionClient.fetch(eq(Plugin.class), eq("apples"))).thenReturn(Optional.of(plugin));
        when(haloPluginManager.startPlugin(any())).thenAnswer((Answer<PluginState>) invocation -> {
            // mock plugin real state is started
            when(pluginWrapper.getPluginState()).thenReturn(PluginState.STARTED);
            return PluginState.STARTED;
        });

        ArgumentCaptor<Plugin> pluginCaptor = doReconcileWithoutRequeue();
        verify(extensionClient, times(3)).update(isA(Plugin.class));

        Plugin updateArgs = pluginCaptor.getAllValues().get(1);
        assertThat(updateArgs).isNotNull();
        assertThat(updateArgs.getSpec().getEnabled()).isTrue();
        assertThat(updateArgs.getStatus().getPhase()).isEqualTo(PluginState.STARTED);
        assertThat(updateArgs.getStatus().getLastStartTime()).isNotNull();
    }

    @Test
    @DisplayName("Reconcile to start failed")
    void reconcileOkWhenPluginManagerStartFailed() {
        Plugin plugin = need2ReconcileForStartupState();

        // mock start plugin failed
        when(extensionClient.fetch(eq(Plugin.class), eq("apples"))).thenReturn(Optional.of(plugin));
        when(haloPluginManager.startPlugin(any())).thenReturn(PluginState.FAILED);

        // mock plugin real state is started
        when(pluginWrapper.getPluginState()).thenReturn(PluginState.STOPPED);
        var pluginDescriptor = mock(PluginDescriptor.class);

        PluginStartingError pluginStartingError =
            PluginStartingError.of("apples", "error message", "dev message");
        when(haloPluginManager.getPluginStartingError(any())).thenReturn(pluginStartingError);

        assertThatThrownBy(() -> {
            ArgumentCaptor<Plugin> pluginCaptor = doReconcileNeedRequeue();

            // Verify the state before the update plugin
            Plugin updateArgs = pluginCaptor.getValue();
            assertThat(updateArgs).isNotNull();
            assertThat(updateArgs.getSpec().getEnabled()).isTrue();

            Plugin.PluginStatus status = updateArgs.getStatus();
            assertThat(status.getPhase()).isEqualTo(PluginState.FAILED);
            assertThat(status.getConditions().peek().getReason()).isEqualTo("error message");
            assertThat(status.getConditions().peek().getMessage()).isEqualTo("dev message");
            assertThat(status.getLastStartTime()).isNull();
        }).isInstanceOf(IllegalStateException.class)
            .hasMessage("error message");

    }

    @Test
    @DisplayName("Reconcile to stop successfully")
    void shouldReconcileStopWhenEnabledIsFalseAndPhaseIsStarted() {
        Plugin plugin = need2ReconcileForStopState();
        when(extensionClient.fetch(eq(Plugin.class), eq("apples"))).thenReturn(Optional.of(plugin));
        when(haloPluginManager.stopPlugin(any())).thenAnswer((Answer<PluginState>) invocation -> {
            when(pluginWrapper.getPluginState()).thenReturn(PluginState.STOPPED);
            return PluginState.STOPPED;
        });
        // mock plugin real state is started
        when(pluginWrapper.getPluginState()).thenReturn(PluginState.STARTED);

        ArgumentCaptor<Plugin> pluginCaptor = doReconcileWithoutRequeue();
        verify(extensionClient, times(3)).update(any(Plugin.class));

        Plugin updateArgs = pluginCaptor.getValue();
        assertThat(updateArgs).isNotNull();
        assertThat(updateArgs.getSpec().getEnabled()).isFalse();
        assertThat(updateArgs.getStatus().getPhase()).isEqualTo(PluginState.STOPPED);
    }

    @Test
    @DisplayName("Reconcile to stop successfully when 'spec.enabled' is inconsistent"
        + " with 'status.phase'")
    void shouldReconcileStopWhenEnabledIsFalseAndPhaseIsStopped() {
        // 模拟插件的实际状态与status.phase记录的状态不一致
        Plugin plugin = JsonUtils.jsonToObject("""
            {
                "apiVersion": "plugin.halo.run/v1alpha1",
                "kind": "Plugin",
                "metadata": {
                    "name": "apples"
                },
                 "spec": {
                    "displayName": "测试插件",
                    "enabled": false
                },
                "status": {
                    "phase": "STOPPED",
                    "loadLocation": "/tmp/plugins/apples.jar"
                }
            }
            """, Plugin.class);
        when(extensionClient.fetch(eq(Plugin.class), eq("apples"))).thenReturn(Optional.of(plugin));
        when(haloPluginManager.stopPlugin(any())).thenAnswer((Answer<PluginState>) invocation -> {
            when(pluginWrapper.getPluginState()).thenReturn(PluginState.STOPPED);
            return PluginState.STOPPED;
        });
        // mock plugin real state is started
        when(pluginWrapper.getPluginState()).thenReturn(PluginState.STARTED);

        ArgumentCaptor<Plugin> pluginCaptor = doReconcileWithoutRequeue();
        verify(extensionClient, times(4)).update(any(Plugin.class));

        Plugin updateArgs = pluginCaptor.getValue();
        assertThat(updateArgs).isNotNull();
        assertThat(updateArgs.getSpec().getEnabled()).isFalse();
        assertThat(updateArgs.getStatus().getPhase()).isEqualTo(PluginState.STOPPED);
    }

    @Test
    @DisplayName("Reconcile to stop failed")
    void shouldReconcileStopWhenEnabledIsFalseAndPhaseIsStartedButStopFailed() {
        Plugin plugin = need2ReconcileForStopState();
        when(extensionClient.fetch(eq(Plugin.class), eq("apples"))).thenReturn(Optional.of(plugin));
        // mock stop failed
        when(haloPluginManager.stopPlugin(any())).thenReturn(PluginState.FAILED);

        // mock plugin real state is started
        when(pluginWrapper.getPluginState()).thenReturn(PluginState.STARTED);

        assertThatThrownBy(() -> {
            ArgumentCaptor<Plugin> pluginCaptor = doReconcileNeedRequeue();

            Plugin updateArgs = pluginCaptor.getValue();
            assertThat(updateArgs).isNotNull();
            assertThat(updateArgs.getSpec().getEnabled()).isFalse();

            Plugin.PluginStatus status = updateArgs.getStatus();
            assertThat(status.getPhase()).isEqualTo(PluginState.FAILED);
        }).isInstanceOf(IllegalStateException.class)
            .hasMessage("Failed to stop plugin: apples");
    }

    @Test
    void recreateDefaultReverseProxyWhenNotExistAndLogoIsPath() throws JSONException {
        Plugin plugin = need2ReconcileForStopState();
        String reverseProxyName = initialReverseProxyName(plugin.getMetadata().getName());
        when(extensionClient.fetch(eq(ReverseProxy.class), eq(reverseProxyName)))
            .thenReturn(Optional.empty());

        plugin.getSpec().setLogo("/logo.png");
        pluginReconciler.recreateDefaultReverseProxy(plugin);
        ArgumentCaptor<ReverseProxy> captor = ArgumentCaptor.forClass(ReverseProxy.class);
        verify(extensionClient, times(1)).create(captor.capture());
        ReverseProxy value = captor.getValue();
        JSONAssert.assertEquals("""
                {
                    "rules": [
                        {
                            "path": "/logo.png",
                            "file": {
                                "filename": "/logo.png"
                            }
                        }
                    ],
                    "apiVersion": "plugin.halo.run/v1alpha1",
                    "kind": "ReverseProxy",
                    "metadata": {
                        "name": "apples-system-generated-reverse-proxy",
                        "labels": {
                            "plugin.halo.run/plugin-name": "apples"
                        }
                    }
                }
                """,
            JsonUtils.objectToJson(value),
            true);
    }

    @Test
    void recreateDefaultReverseProxyWhenNotExistAndLogoIsAbsolute() {
        Plugin plugin = need2ReconcileForStopState();
        String reverseProxyName = initialReverseProxyName(plugin.getMetadata().getName());
        when(extensionClient.fetch(eq(ReverseProxy.class), eq(reverseProxyName)))
            .thenReturn(Optional.empty());

        plugin.getSpec().setLogo("http://example.com/logo");
        pluginReconciler.recreateDefaultReverseProxy(plugin);
        ArgumentCaptor<ReverseProxy> captor = ArgumentCaptor.forClass(ReverseProxy.class);
        verify(extensionClient, times(1)).create(captor.capture());
        ReverseProxy value = captor.getValue();
        assertThat(value.getRules()).isEmpty();
    }

    @Test
    void recreateDefaultReverseProxyWhenExist() {
        Plugin plugin = need2ReconcileForStopState();
        plugin.getSpec().setLogo("/logo.png");

        String reverseProxyName = initialReverseProxyName(plugin.getMetadata().getName());
        ReverseProxy reverseProxy = new ReverseProxy();
        reverseProxy.setMetadata(new Metadata());
        reverseProxy.getMetadata().setName(reverseProxyName);
        reverseProxy.setRules(new ArrayList<>());

        when(extensionClient.fetch(eq(ReverseProxy.class), eq(reverseProxyName)))
            .thenReturn(Optional.of(reverseProxy));

        pluginReconciler.recreateDefaultReverseProxy(plugin);
        verify(extensionClient).update(any());
    }

    @Nested
    class PluginLogoTest {

        @Test
        void absoluteUri() {
            Plugin plugin = new Plugin();
            plugin.setSpec(new Plugin.PluginSpec());
            plugin.getSpec().setLogo("https://example.com/logo.png");
            plugin.getSpec().setVersion("1.0.0");
            String logo = pluginReconciler.generateAccessibleLogoUrl(plugin);
            assertThat(logo).isEqualTo("https://example.com/logo.png");
        }

        @Test
        void absoluteUriWithQueryParam() {
            Plugin plugin = new Plugin();
            plugin.setSpec(new Plugin.PluginSpec());
            plugin.getSpec().setLogo("https://example.com/logo.png?hello=world");
            plugin.getSpec().setVersion("1.0.0");
            assertThat(pluginReconciler.generateAccessibleLogoUrl(plugin))
                .isEqualTo("https://example.com/logo.png?hello=world");
        }

        @Test
        void logoIsNull() {
            Plugin plugin = new Plugin();
            plugin.setSpec(new Plugin.PluginSpec());
            plugin.getSpec().setLogo(null);
            plugin.getSpec().setVersion("1.0.0");
            assertThat(pluginReconciler.generateAccessibleLogoUrl(plugin)).isNull();
        }

        @Test
        void logoIsEmpty() {
            Plugin plugin = new Plugin();
            plugin.setSpec(new Plugin.PluginSpec());
            plugin.getSpec().setLogo("");
            plugin.getSpec().setVersion("1.0.0");
            assertThat(pluginReconciler.generateAccessibleLogoUrl(plugin)).isNull();
        }

        @Test
        void relativePath() {
            Plugin plugin = new Plugin();
            plugin.setSpec(new Plugin.PluginSpec());
            plugin.setMetadata(new Metadata());
            plugin.getMetadata().setName("fake-plugin");
            plugin.getSpec().setLogo("/static/logo.jpg");
            plugin.getSpec().setVersion("1.0.0");
            assertThat(pluginReconciler.generateAccessibleLogoUrl(plugin))
                .isEqualTo("/plugins/fake-plugin/assets/static/logo.jpg?version=1.0.0");
        }

        @Test
        void dataBlob() {
            Plugin plugin = new Plugin();
            plugin.setSpec(new Plugin.PluginSpec());
            plugin.setMetadata(new Metadata());
            plugin.getMetadata().setName("fake-plugin");
            plugin.getSpec().setLogo("data:image/gif;base64,R0lGODfake");
            plugin.getSpec().setVersion("2.0.0");
            assertThat(pluginReconciler.generateAccessibleLogoUrl(plugin))
                .isEqualTo("data:image/gif;base64,R0lGODfake");
        }
    }

    @Test
    void resolvePluginPathAnnotation() {
        var pluginRoot = Paths.get("tmp", "plugins");
        when(haloPluginManager.getPluginsRoot()).thenReturn(pluginRoot);
        var path = pluginReconciler.resolvePluginPathForAnno(
            pluginRoot.resolve("sitemap-1.0.jar").toString());
        assertThat(path).isEqualTo("sitemap-1.0.jar");

        var givenPath = Paths.get("abc", "plugins", "sitemap-1.0.jar");
        path = pluginReconciler.resolvePluginPathForAnno(givenPath.toString());
        assertThat(path).isEqualTo(givenPath.toString());
    }

    @Nested
    class ReloadPluginTest {
        private static final String PLUGIN_NAME = "fake-plugin";
        private static final Path OLD_PLUGIN_PATH = Paths.get("/path/to/old/plugin.jar");

        @Test
        void reload() throws IOException, URISyntaxException {
            var fakePluginUri = requireNonNull(
                getClass().getClassLoader().getResource("plugin/plugin-0.0.2")).toURI();
            Path tempDirectory = Files.createTempDirectory("halo-ut-plugin-service-impl-");
            final Path fakePluginPath = tempDirectory.resolve("plugin-0.0.2.jar");
            try {
                FileUtils.jar(Paths.get(fakePluginUri), tempDirectory.resolve("plugin-0.0.2.jar"));
                when(haloPluginManager.getPluginsRoot()).thenReturn(tempDirectory);
                // mock plugin
                Plugin plugin = mock(Plugin.class);
                Metadata metadata = new Metadata();
                metadata.setName(PLUGIN_NAME);
                when(plugin.getMetadata()).thenReturn(metadata);
                metadata.setAnnotations(new HashMap<>());
                metadata.getAnnotations()
                    .put(PluginConst.RELOAD_ANNO, fakePluginPath.toString());
                Plugin.PluginStatus pluginStatus = mock(Plugin.PluginStatus.class);
                when(pluginStatus.getLoadLocation()).thenReturn(OLD_PLUGIN_PATH.toUri());
                when(plugin.statusNonNull()).thenReturn(pluginStatus);

                when(extensionClient.fetch(Plugin.class, PLUGIN_NAME))
                    .thenReturn(Optional.of(plugin));

                // call reload method
                pluginReconciler.reload(plugin);

                // verify that the plugin is updated with the new plugin's spec, annotations, and
                // labels
                verify(plugin).setSpec(any(Plugin.PluginSpec.class));
                verify(extensionClient).update(plugin);

                // verify that the plugin's load location is updated to the new plugin path
                verify(pluginStatus).setLoadLocation(fakePluginPath.toUri());

                // verify that the new plugin is reloaded
                verify(haloPluginManager).reloadPluginWithPath(PLUGIN_NAME, fakePluginPath);
            } finally {
                FileUtils.deleteRecursivelyAndSilently(tempDirectory);
            }
        }

        @Test
        void shouldDeleteFile() throws IOException {
            String newPluginPath = "/path/to/new/plugin.jar";

            // Case 1: oldPluginLocation is null
            assertFalse(pluginReconciler.shouldDeleteFile(newPluginPath, null));

            // Case 2: oldPluginLocation is the same as newPluginPath
            assertFalse(pluginReconciler.shouldDeleteFile(newPluginPath,
                pluginReconciler.toUri(newPluginPath)));

            Path tempDirectory = Files.createTempDirectory("halo-ut-plugin-service-impl-");
            try {
                Path oldPluginPath = tempDirectory.resolve("plugin.jar");
                final URI oldPluginLocation = oldPluginPath.toUri();
                Files.createFile(oldPluginPath);
                // Case 3: oldPluginLocation is different from newPluginPath and is a JAR file
                assertTrue(pluginReconciler.shouldDeleteFile(newPluginPath, oldPluginLocation));
            } finally {
                FileUtils.deleteRecursivelyAndSilently(tempDirectory);
            }

            // Case 4: oldPluginLocation is different from newPluginPath and is not a JAR file
            assertFalse(pluginReconciler.shouldDeleteFile(newPluginPath,
                Paths.get("/path/to/old/plugin.txt").toUri()));
        }

        @Test
        void toPath() {
            assertThat(pluginReconciler.toPath(null)).isNull();
            assertThat(pluginReconciler.toPath("")).isNull();
            assertThat(pluginReconciler.toPath(" ")).isNull();

            final var filePath = Paths.get("path", "to", "file.txt").toAbsolutePath();

            // test for file:///
            assertEquals(filePath, pluginReconciler.toPath(filePath.toUri().toString()));
            // test for absolute path /home/xyz or C:\Windows
            assertEquals(filePath, pluginReconciler.toPath(filePath.toString()));

            var exception = assertThrows(IllegalArgumentException.class, () -> {
                var fileUri = filePath.toUri();
                pluginReconciler.toPath(fileUri.toString().replaceFirst("file", "http"));
            });
            assertTrue(exception.getMessage().contains("not reside in the file system"));
        }

        @Test
        void toUri() {
            // Test with null pathString
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pluginReconciler.toUri(null);
            });

            // Test with empty pathString
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                pluginReconciler.toUri("");
            });

            // Test with non-empty pathString
            var filePath = Paths.get("path", "to", "file");
            URI uri = pluginReconciler.toUri(filePath.toString());
            assertEquals(filePath.toUri(), uri);
        }
    }

    @Test
    void persistenceFailureStatus() {
        String name = "fake-plugin";
        Plugin plugin = new Plugin();
        Plugin.PluginStatus status = new Plugin.PluginStatus();
        plugin.setMetadata(new Metadata());
        plugin.getMetadata().setName(name);
        plugin.setStatus(status);
        when(extensionClient.fetch(eq(Plugin.class), eq(name)))
            .thenReturn(Optional.of(plugin));
        PluginWrapper pluginWrapper = mock(PluginWrapper.class);
        when(pluginWrapper.getPluginPath()).thenReturn(Paths.get("/path/to/plugin.jar"));
        when(haloPluginManager.getPlugin(eq(name)))
            .thenReturn(pluginWrapper);
        Throwable error = mock(Throwable.class);
        pluginReconciler.persistenceFailureStatus(name, error);

        assertThat(status.getPhase()).isEqualTo(PluginState.FAILED);
        assertThat(status.getConditions()).hasSize(1);
        assertThat(status.getConditions().peek().getType())
            .isEqualTo(PluginState.FAILED.toString());

        verify(pluginWrapper).setPluginState(eq(PluginState.FAILED));
        verify(pluginWrapper).setFailedException(eq(error));
    }

    @Test
    void shouldReconcileStartState() {
        Plugin plugin = new Plugin();
        plugin.setMetadata(new Metadata());
        plugin.getMetadata().setName("fake-plugin");
        plugin.setSpec(new Plugin.PluginSpec());

        PluginWrapper pluginWrapper = mock(PluginWrapper.class);
        when(haloPluginManager.getPlugin(eq("fake-plugin"))).thenReturn(pluginWrapper);

        plugin.getSpec().setEnabled(false);
        assertThat(pluginReconciler.shouldReconcileStartState(plugin)).isFalse();

        plugin.getSpec().setEnabled(true);
        plugin.statusNonNull().setPhase(PluginState.RESOLVED);
        assertThat(pluginReconciler.shouldReconcileStartState(plugin)).isTrue();

        when(pluginWrapper.getPluginState()).thenReturn(PluginState.STOPPED);
        assertThat(pluginReconciler.shouldReconcileStartState(plugin)).isTrue();

        plugin.statusNonNull().setPhase(PluginState.STARTED);
        when(pluginWrapper.getPluginState()).thenReturn(PluginState.STARTED);
        assertThat(pluginReconciler.shouldReconcileStartState(plugin)).isFalse();
    }

    @Test
    void shouldReconcileStopState() {
        Plugin plugin = new Plugin();
        plugin.setMetadata(new Metadata());
        plugin.getMetadata().setName("fake-plugin");
        plugin.setSpec(new Plugin.PluginSpec());

        PluginWrapper pluginWrapper = mock(PluginWrapper.class);
        when(haloPluginManager.getPlugin(eq("fake-plugin"))).thenReturn(pluginWrapper);

        plugin.getSpec().setEnabled(true);
        assertThat(pluginReconciler.shouldReconcileStopState(plugin)).isFalse();

        plugin.getSpec().setEnabled(false);
        plugin.statusNonNull().setPhase(PluginState.RESOLVED);
        assertThat(pluginReconciler.shouldReconcileStopState(plugin)).isTrue();

        when(pluginWrapper.getPluginState()).thenReturn(PluginState.STOPPED);
        assertThat(pluginReconciler.shouldReconcileStopState(plugin)).isTrue();

        plugin.statusNonNull().setPhase(PluginState.STOPPED);
        when(pluginWrapper.getPluginState()).thenReturn(PluginState.STOPPED);
        assertThat(pluginReconciler.shouldReconcileStopState(plugin)).isFalse();
    }

    private ArgumentCaptor<Plugin> doReconcileNeedRequeue() {
        ArgumentCaptor<Plugin> pluginCaptor = ArgumentCaptor.forClass(Plugin.class);
        doNothing().when(extensionClient).update(pluginCaptor.capture());

        // reconcile
        Reconciler.Result result = pluginReconciler.reconcile(new Reconciler.Request("apples"));
        assertThat(result).isNotNull();
        assertThat(result.reEnqueue()).isEqualTo(true);

        verify(extensionClient, times(2)).update(any());
        return pluginCaptor;
    }

    private ArgumentCaptor<Plugin> doReconcileWithoutRequeue() {
        ArgumentCaptor<Plugin> pluginCaptor = ArgumentCaptor.forClass(Plugin.class);
        doNothing().when(extensionClient).update(pluginCaptor.capture());

        // reconcile
        Reconciler.Result result = pluginReconciler.reconcile(new Reconciler.Request("apples"));
        assertThat(result).isNotNull();
        assertThat(result.reEnqueue()).isEqualTo(false);
        return pluginCaptor;
    }

    private Plugin need2ReconcileForStartupState() {
        return JsonUtils.jsonToObject("""
            {
                "apiVersion": "plugin.halo.run/v1alpha1",
                "kind": "Plugin",
                "metadata": {
                    "name": "apples"
                },
                 "spec": {
                    "displayName": "测试插件",
                    "enabled": true
                },
                "status": {
                    "phase": "STOPPED",
                    "loadLocation": "/tmp/plugins/apples.jar"
                }
            }
            """, Plugin.class);
    }

    private Plugin need2ReconcileForStopState() {
        return JsonUtils.jsonToObject("""
            {
                "apiVersion": "plugin.halo.run/v1alpha1",
                "kind": "Plugin",
                "metadata": {
                    "name": "apples"
                },
                 "spec": {
                    "displayName": "测试插件",
                    "enabled": false
                },
                "status": {
                    "phase": "STARTED",
                    "loadLocation": "/tmp/plugins/apples.jar"
                }
            }
            """, Plugin.class);
    }
}