package run.halo.app.plugin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.zafarkhaja.semver.Version;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;
import org.springframework.context.ApplicationContext;
import run.halo.app.infra.SystemVersionSupplier;

@ExtendWith(MockitoExtension.class)
class HaloPluginManagerTest {

    @Mock
    PluginProperties pluginProperties;

    @Mock
    SystemVersionSupplier systemVersionSupplier;

    @Mock
    PluginsRootGetter pluginsRootGetter;

    @Mock
    ApplicationContext rootContext;

    @TempDir
    Path tempDir;

    HaloPluginManager pluginManager;

    @BeforeEach
    void setUp() throws Exception {
        when(pluginProperties.getRuntimeMode()).thenReturn(RuntimeMode.DEPLOYMENT);
        when(systemVersionSupplier.get()).thenReturn(Version.of(1, 2, 3));
        when(pluginsRootGetter.get()).thenReturn(tempDir);

        pluginManager =
                spy(new HaloPluginManager(rootContext, pluginProperties, systemVersionSupplier, pluginsRootGetter));
        pluginManager.afterPropertiesSet();
    }

    @Test
    void shouldGetDependentsWhilePluginsNotResolved() {
        var dependents = pluginManager.getDependents("fake-plugin");
        assertEquals(0, dependents.size());
    }

    @Test
    void shouldGetDependentsWhilePluginsResolved() {
        pluginManager.loadPlugins();
        var dependents = pluginManager.getDependents("fake-plugin");
        assertEquals(0, dependents.size());
    }

    @Test
    void stopPluginsShouldStopAllStartedPlugins() {
        // Dependency ordering (dependents before dependencies) is handled by
        // PF4J's stopPlugin itself, so here we only assert every started plugin
        // is attempted.
        doReturn(List.of(mockPluginWrapper("plugin-a"), mockPluginWrapper("plugin-b")))
                .when(pluginManager)
                .startedPlugins();
        doReturn(PluginState.STOPPED).when(pluginManager).stopPlugin(anyString());

        pluginManager.stopPlugins();

        verify(pluginManager).stopPlugin("plugin-a");
        verify(pluginManager).stopPlugin("plugin-b");
    }

    @Test
    void stopPluginsShouldHandleEmptyStartedPlugins() {
        doReturn(List.of()).when(pluginManager).startedPlugins();

        assertDoesNotThrow(() -> pluginManager.stopPlugins());

        verify(pluginManager, never()).stopPlugin(anyString());
    }

    @Test
    void stopPluginsShouldContinueOnStopFailure() {
        doReturn(List.of(mockPluginWrapper("plugin-1"), mockPluginWrapper("plugin-2")))
                .when(pluginManager)
                .startedPlugins();
        // First plugin fails to stop
        doThrow(new RuntimeException("stop failed")).when(pluginManager).stopPlugin("plugin-1");
        doReturn(PluginState.STOPPED).when(pluginManager).stopPlugin("plugin-2");

        assertDoesNotThrow(() -> pluginManager.stopPlugins());

        // Both should be attempted
        verify(pluginManager).stopPlugin("plugin-1");
        verify(pluginManager).stopPlugin("plugin-2");
    }

    @Test
    void stopPluginsShouldOnlyStopStartedPlugins() {
        // Only started plugins are returned
        doReturn(List.of(mockPluginWrapper("plugin-started")))
                .when(pluginManager)
                .startedPlugins();
        doReturn(PluginState.STOPPED).when(pluginManager).stopPlugin(anyString());

        pluginManager.stopPlugins();

        verify(pluginManager, times(1)).stopPlugin(anyString());
    }

    @Test
    void unloadPluginsShouldUnloadAllResolvedPlugins() {
        doReturn(List.of(mockPluginWrapper("plugin-a"), mockPluginWrapper("plugin-b")))
                .when(pluginManager)
                .getResolvedPlugins();
        doReturn(true).when(pluginManager).unloadPlugin(anyString());

        pluginManager.unloadPlugins();

        verify(pluginManager).unloadPlugin("plugin-a");
        verify(pluginManager).unloadPlugin("plugin-b");
    }

    @Test
    void unloadPluginsShouldContinueOnUnloadFailure() {
        doReturn(List.of(mockPluginWrapper("plugin-1"), mockPluginWrapper("plugin-2")))
                .when(pluginManager)
                .getResolvedPlugins();
        // First plugin fails to unload
        doThrow(new RuntimeException("unload failed")).when(pluginManager).unloadPlugin("plugin-1");
        doReturn(true).when(pluginManager).unloadPlugin("plugin-2");

        assertDoesNotThrow(() -> pluginManager.unloadPlugins());

        // Both should be attempted
        verify(pluginManager).unloadPlugin("plugin-1");
        verify(pluginManager).unloadPlugin("plugin-2");
    }

    @Test
    void destroyShouldStopAndUnloadPlugins() {
        doReturn(List.of()).when(pluginManager).startedPlugins();
        doReturn(List.of()).when(pluginManager).getResolvedPlugins();

        assertDoesNotThrow(() -> pluginManager.destroy());

        var inOrder = inOrder(pluginManager);
        inOrder.verify(pluginManager).stopPlugins();
        inOrder.verify(pluginManager).unloadPlugins();
    }

    @Test
    void startPluginsShouldThrowUnsupportedOperationException() {
        // startPlugins must still throw — bulk start is explicitly unsupported
        assertThrows(UnsupportedOperationException.class, () -> pluginManager.startPlugins());
    }

    private static PluginWrapper mockPluginWrapper(String pluginId) {
        var pw = mock(PluginWrapper.class);
        when(pw.getPluginId()).thenReturn(pluginId);
        return pw;
    }
}
