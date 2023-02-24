package run.halo.app.core.extension.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.core.extension.reconciler.PluginReconciler.initialReverseProxyName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.context.ApplicationEventPublisher;
import run.halo.app.core.extension.Plugin;
import run.halo.app.core.extension.ReverseProxy;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.plugin.HaloPluginManager;
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
    PluginWrapper pluginWrapper;

    @Mock
    ApplicationEventPublisher eventPublisher;

    PluginReconciler pluginReconciler;

    @BeforeEach
    void setUp() {
        pluginReconciler = new PluginReconciler(extensionClient, haloPluginManager, eventPublisher);
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
        when(extensionClient.fetch(eq(Plugin.class), eq("apples"))).thenReturn(Optional.of(plugin));
        when(haloPluginManager.startPlugin(any())).thenAnswer((Answer<PluginState>) invocation -> {
            // mock plugin real state is started
            when(pluginWrapper.getPluginState()).thenReturn(PluginState.STARTED);
            return PluginState.STARTED;
        });

        ArgumentCaptor<Plugin> pluginCaptor = doReconcileWithoutRequeue();
        verify(extensionClient, times(2)).update(isA(Plugin.class));

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
        when(haloPluginManager.startPlugin(any())).thenAnswer((Answer<PluginState>) invocation -> {
            // mock plugin real state is started
            when(pluginWrapper.getPluginState()).thenReturn(PluginState.FAILED);
            return PluginState.FAILED;
        });

        // mock plugin real state is started
        when(pluginWrapper.getPluginState()).thenReturn(PluginState.STOPPED);

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
        verify(extensionClient, times(2)).update(any(Plugin.class));

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
                    "phase": "STOPPED"
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
        verify(extensionClient, times(2)).update(any(Plugin.class));

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
    void createInitialReverseProxyWhenNotExistAndLogoIsPath() throws JSONException {
        Plugin plugin = need2ReconcileForStopState();
        String reverseProxyName = initialReverseProxyName(plugin.getMetadata().getName());
        when(extensionClient.fetch(eq(ReverseProxy.class), eq(reverseProxyName)))
            .thenReturn(Optional.empty());

        plugin.getSpec().setLogo("/logo.png");
        pluginReconciler.createInitialReverseProxyIfNotPresent(plugin);
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
    void createInitialReverseProxyWhenNotExistAndLogoIsAbsolute() {
        Plugin plugin = need2ReconcileForStopState();
        String reverseProxyName = initialReverseProxyName(plugin.getMetadata().getName());
        when(extensionClient.fetch(eq(ReverseProxy.class), eq(reverseProxyName)))
            .thenReturn(Optional.empty());

        plugin.getSpec().setLogo("http://example.com/logo");
        pluginReconciler.createInitialReverseProxyIfNotPresent(plugin);
        ArgumentCaptor<ReverseProxy> captor = ArgumentCaptor.forClass(ReverseProxy.class);
        verify(extensionClient, times(1)).create(captor.capture());
        ReverseProxy value = captor.getValue();
        assertThat(value.getRules()).isEmpty();
    }

    @Test
    void createInitialReverseProxyWhenExist() {
        Plugin plugin = need2ReconcileForStopState();
        plugin.getSpec().setLogo("/logo.png");

        String reverseProxyName = initialReverseProxyName(plugin.getMetadata().getName());
        ReverseProxy reverseProxy = new ReverseProxy();
        reverseProxy.setMetadata(new Metadata());
        reverseProxy.getMetadata().setName(reverseProxyName);
        reverseProxy.setRules(new ArrayList<>());

        when(extensionClient.fetch(eq(ReverseProxy.class), eq(reverseProxyName)))
            .thenReturn(Optional.of(reverseProxy));
        when(pluginWrapper.getRuntimeMode()).thenReturn(RuntimeMode.DEPLOYMENT);

        pluginReconciler.createInitialReverseProxyIfNotPresent(plugin);
        verify(extensionClient, times(0)).update(any());

        when(pluginWrapper.getRuntimeMode()).thenReturn(RuntimeMode.DEVELOPMENT);
        pluginReconciler.createInitialReverseProxyIfNotPresent(plugin);
        verify(extensionClient, times(1)).update(any());
    }

    @Nested
    class PluginLogoTest {

        @Test
        void absoluteUri() {
            Plugin plugin = new Plugin();
            plugin.setSpec(new Plugin.PluginSpec());
            plugin.getSpec().setLogo("https://example.com/logo.png");
            plugin.getSpec().setVersion("1.0.0");
            pluginReconciler.generateAccessibleLogoUrl(plugin);
            assertThat(plugin.statusNonNull().getLogo())
                .isEqualTo("https://example.com/logo.png");
        }

        @Test
        void absoluteUriWithQueryParam() {
            Plugin plugin = new Plugin();
            plugin.setSpec(new Plugin.PluginSpec());
            plugin.getSpec().setLogo("https://example.com/logo.png?hello=world");
            plugin.getSpec().setVersion("1.0.0");
            pluginReconciler.generateAccessibleLogoUrl(plugin);
            assertThat(plugin.statusNonNull().getLogo())
                .isEqualTo("https://example.com/logo.png?hello=world");
        }

        @Test
        void logoIsNull() {
            Plugin plugin = new Plugin();
            plugin.setSpec(new Plugin.PluginSpec());
            plugin.getSpec().setLogo(null);
            plugin.getSpec().setVersion("1.0.0");
            pluginReconciler.generateAccessibleLogoUrl(plugin);
            assertThat(plugin.statusNonNull().getLogo()).isNull();
        }

        @Test
        void logoIsEmpty() {
            Plugin plugin = new Plugin();
            plugin.setSpec(new Plugin.PluginSpec());
            plugin.getSpec().setLogo("");
            plugin.getSpec().setVersion("1.0.0");
            pluginReconciler.generateAccessibleLogoUrl(plugin);
            assertThat(plugin.statusNonNull().getLogo()).isNull();
        }

        @Test
        void relativePath() {
            Plugin plugin = new Plugin();
            plugin.setSpec(new Plugin.PluginSpec());
            plugin.setMetadata(new Metadata());
            plugin.getMetadata().setName("fake-plugin");
            plugin.getSpec().setLogo("/static/logo.jpg");
            plugin.getSpec().setVersion("1.0.0");
            pluginReconciler.generateAccessibleLogoUrl(plugin);
            assertThat(plugin.statusNonNull().getLogo())
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
            pluginReconciler.generateAccessibleLogoUrl(plugin);
            assertThat(plugin.statusNonNull().getLogo())
                .isEqualTo("data:image/gif;base64,R0lGODfake");
        }
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
                    "phase": "STOPPED"
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
                    "phase": "STARTED"
                }
            }
            """, Plugin.class);
    }
}