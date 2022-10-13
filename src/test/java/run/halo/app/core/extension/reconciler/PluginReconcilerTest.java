package run.halo.app.core.extension.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ExtensionClient;
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

    PluginReconciler pluginReconciler;

    @BeforeEach
    void setUp() {
        pluginReconciler = new PluginReconciler(extensionClient, haloPluginManager);

        when(haloPluginManager.getPlugin(any())).thenReturn(pluginWrapper);
        when(haloPluginManager.getUnresolvedPlugins()).thenReturn(List.of());
    }

    @Test
    @DisplayName("Reconcile to start successfully")
    void reconcileOkWhenPluginManagerStartSuccessfully() {
        Plugin plugin = need2ReconcileForStartupState();
        when(extensionClient.fetch(eq(Plugin.class), eq("apples"))).thenReturn(Optional.of(plugin));
        when(haloPluginManager.startPlugin(any())).thenReturn(PluginState.STARTED);
        // mock plugin real state is started
        when(pluginWrapper.getPluginState()).thenReturn(PluginState.STOPPED);

        ArgumentCaptor<Plugin> pluginCaptor = doReconcileWithoutRequeue();

        Plugin updateArgs = pluginCaptor.getValue();
        assertThat(updateArgs).isNotNull();
        assertThat(updateArgs.getSpec().getEnabled()).isTrue();
        assertThat(updateArgs.getStatus().getPhase()).isEqualTo(PluginState.STARTED);
        assertThat(updateArgs.getStatus().getLastStartTime()).isNotNull();
    }

    @Test
    @DisplayName("Reconcile to start failed")
    void reconcileOkWhenPluginManagerStartFailed() {
        Plugin plugin = need2ReconcileForStartupState();
        when(extensionClient.fetch(eq(Plugin.class), eq("apples"))).thenReturn(Optional.of(plugin));

        // mock start plugin failed
        when(haloPluginManager.startPlugin(any())).thenReturn(PluginState.FAILED);
        // mock plugin real state is started
        when(pluginWrapper.getPluginState()).thenReturn(PluginState.STOPPED);

        PluginStartingError pluginStartingError =
            PluginStartingError.of("apples", "error message", "dev message");
        when(haloPluginManager.getPluginStartingError(any())).thenReturn(pluginStartingError);

        ArgumentCaptor<Plugin> pluginCaptor = doReconcileNeedRequeue();

        // Verify the state before the update plugin
        Plugin updateArgs = pluginCaptor.getValue();
        assertThat(updateArgs).isNotNull();
        assertThat(updateArgs.getSpec().getEnabled()).isTrue();

        Plugin.PluginStatus status = updateArgs.getStatus();
        assertThat(status.getPhase()).isEqualTo(PluginState.FAILED);
        assertThat(status.getReason()).isEqualTo("error message");
        assertThat(status.getMessage()).isEqualTo("dev message");
        assertThat(status.getLastStartTime()).isNull();
    }

    @Test
    @DisplayName("Reconcile to stop successfully")
    void shouldReconcileStopWhenEnabledIsFalseAndPhaseIsStarted() {
        Plugin plugin = need2ReconcileForStopState();
        when(extensionClient.fetch(eq(Plugin.class), eq("apples"))).thenReturn(Optional.of(plugin));
        when(haloPluginManager.stopPlugin(any())).thenReturn(PluginState.STOPPED);
        // mock plugin real state is started
        when(pluginWrapper.getPluginState()).thenReturn(PluginState.STARTED);

        ArgumentCaptor<Plugin> pluginCaptor = doReconcileWithoutRequeue();

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
        when(haloPluginManager.stopPlugin(any())).thenReturn(PluginState.STOPPED);
        // mock plugin real state is started
        when(pluginWrapper.getPluginState()).thenReturn(PluginState.STARTED);

        ArgumentCaptor<Plugin> pluginCaptor = doReconcileWithoutRequeue();

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

        // mock stop failed message
        PluginStartingError pluginStartingError =
            PluginStartingError.of("apples", "error message", "dev message");
        when(haloPluginManager.getPluginStartingError(any())).thenReturn(pluginStartingError);

        ArgumentCaptor<Plugin> pluginCaptor = doReconcileNeedRequeue();

        Plugin updateArgs = pluginCaptor.getValue();
        assertThat(updateArgs).isNotNull();
        assertThat(updateArgs.getSpec().getEnabled()).isFalse();

        Plugin.PluginStatus status = updateArgs.getStatus();
        assertThat(status.getPhase()).isEqualTo(PluginState.FAILED);
        assertThat(status.getReason()).isEqualTo("error message");
        assertThat(status.getMessage()).isEqualTo("dev message");
    }

    private ArgumentCaptor<Plugin> doReconcileNeedRequeue() {
        ArgumentCaptor<Plugin> pluginCaptor = ArgumentCaptor.forClass(Plugin.class);
        doNothing().when(extensionClient).update(pluginCaptor.capture());

        // reconcile
        Reconciler.Result result = pluginReconciler.reconcile(new Reconciler.Request("apples"));
        assertThat(result).isNotNull();
        assertThat(result.reEnqueue()).isEqualTo(true);

        verify(extensionClient, times(1)).update(any());
        return pluginCaptor;
    }

    private ArgumentCaptor<Plugin> doReconcileWithoutRequeue() {
        ArgumentCaptor<Plugin> pluginCaptor = ArgumentCaptor.forClass(Plugin.class);
        doNothing().when(extensionClient).update(pluginCaptor.capture());

        // reconcile
        Reconciler.Result result = pluginReconciler.reconcile(new Reconciler.Request("apples"));
        assertThat(result).isNotNull();
        assertThat(result.reEnqueue()).isEqualTo(false);

        verify(extensionClient, times(1)).update(any());
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